package ru.objects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Sergei on 9/9/2016.
 */
public class ChatClientNIO extends java.util.Observable implements Runnable {
	private SocketChannel socketChannel;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    
    private RSA rsa;
    private RC4 rc4;
    private final BigInteger K;
    private final BigInteger privateWord;
    
    private BlockingQueue<String> queueIn = new ArrayBlockingQueue<String>(5);
    
    private Charset ch = Charset.forName("UTF-8");
    private CharsetDecoder decoder = ch.newDecoder();
    
	private String Reply;
        
    public ChatClientNIO(String address, int port, String password) throws IOException {
        selector = SelectorProvider.provider().openSelector();
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(address, port));
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        rsa = new RSA();
        rc4 = new RC4(password.getBytes());

        SecureRandom rnd = new SecureRandom();
        rnd.setSeed(System.currentTimeMillis());
        K = new BigInteger(1024, rnd);
        privateWord = new BigInteger(256, rnd);
    }
	
    private void connect(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
        System.out.println("Connect to " + socketChannel.getRemoteAddress());
    }
    
    
    public void sendMessage(String type, String attachment) throws InterruptedException {
    	JsonObject reply = new JsonObject();
        reply.addProperty("type", type);
        reply.addProperty("attachment", rc4.DoIt(new BigInteger(attachment.getBytes())));
        queueIn.put(new Gson().toJson(reply));    
        
        System.out.println(reply);
        
        SelectionKey key = socketChannel.keyFor(selector);
        key.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }
        
    private void read(SelectionKey key) throws IOException, InterruptedException {
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();
        CharBuffer buff = decoder.decode(buffer);
        
        JsonReader reader = new JsonReader(new StringReader(String.valueOf(buff)));
        reader.setLenient(true);
        Message message = new Gson().fromJson(reader, Message.class);
        System.out.println(message);
        
        if(message == null || message.equals("null")) return;
        
        switch (message.getType()) {
            case "message":            	
                Reply = message.getName() + " " + new String(rc4.DoIt(new BigInteger(message.getAttachment())).toByteArray());
                this.setChanged();
                this.notifyObservers();
                break;
            case "EKE-1.1":
                rsa.setModulus(new BigInteger(message.getAttachment()));
                break;
            case "EKE-1.2":            	
            	BigInteger code = new BigInteger(message.getAttachment());
            	code = rc4.DoIt(code);
            	rsa.setPublicKey(code);
            	BigInteger codeK = rsa.encrypt(K);
            	codeK = rc4.DoIt(codeK);
            	sendMessage("EKE-1", codeK.toString());
            	rc4 = new RC4(K.toByteArray());
                break;
            case "EKE-2":
                sendMessage("EKE-2.1", rc4.DoIt(rc4.DoIt(new BigInteger(message.getAttachment()))).toString());
                Thread.currentThread().sleep(100);
                sendMessage("EKE-2.2", rc4.DoIt(privateWord).toString());
                break;
            case "EKE-3":
            	BigInteger word = rc4.DoIt(new BigInteger(message.getAttachment()));
            	
            	System.out.println("Word: " + word);
            	System.out.println("privateWord: " + privateWord);
            	
            	if(!privateWord.equals(word)) {
            		System.out.println("Error: EKE-3. Server private str 2 != user private str 2");
                    System.exit(-18);
            	}
                System.out.println("Server private str 1 == user private str 1");
                break;
            default:
                System.out.println("Wrong type for EKE.");
                break;
        }
    }
    
    public String getMessages() {
        return Reply;
    }
    
	@Override
	public void run() {
		try {
            while (selector.select() != -1) {
                Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();
                    
                    if (!key.isValid()) {
                    	continue;
                    }
                    if (key.isConnectable()) {
                        connect(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                    	while(!queueIn.isEmpty()) {
	                        String message = queueIn.poll();	       
	                        if (message != null) {
	                            socketChannel.write(ByteBuffer.wrap(message.getBytes(ch)));
	                            key.interestOps(SelectionKey.OP_READ);
	                        }
                    	}
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
		
	}
    
	class Message {
		String type;
		String name;
		String attachment;
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getAttachment() {
			return attachment;
		}
		public void setAttachment(String attachment) {
			this.attachment = attachment;
		}
		
		@Override
		public String toString() {
			return "Message [type=" + type + ", name=" + name + ", attachment=" + attachment + "]";
		}
	}
	
}

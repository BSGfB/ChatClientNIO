package ru.objects;

import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import com.google.gson.JsonObject;

import ru.objects.ChatClientNIO;

public class ClientConsole implements Observer {
	private Scanner scan = new Scanner(System.in);
	ChatClientNIO client;
	
	public ClientConsole(ChatClientNIO client) {
		this.client = client;
		this.client.addObserver(this);		
	}

	public void Run() {
		while(true) {
			String str = scan.nextLine();
			
			if("EXIT".equals(str))
				System.exit(0);
			else if(str == null)
				continue;
			else if(str.length() == 0)
				continue;
			else {
				try {
					client.sendMessage("message", str);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
						
		}
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		ChatClientNIO client = (ChatClientNIO)arg0;
		System.out.println(client.getMessages());
	}
	

}

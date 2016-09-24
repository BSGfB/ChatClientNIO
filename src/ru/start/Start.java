package ru.start;

import java.io.IOException;

import ru.objects.ChatClientNIO;
import ru.objects.ClientConsole;

public class Start {

	public static void main(String[] args) {
		ChatClientNIO chat;
		try {
			chat = new ChatClientNIO("localhost", 2020, "99538574");
			new Thread(chat).start();
			
			ClientConsole cl = new ClientConsole(chat);
			cl.Run();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

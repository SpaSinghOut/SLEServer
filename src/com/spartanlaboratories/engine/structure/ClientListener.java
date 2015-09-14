package com.spartanlaboratories.engine.structure;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.spartanlaboratories.engine.util.Location;

class ClientListener{
	Socket client;
	PrintWriter out;
	BufferedReader in;
	HumanClient human;
	int quadInfoLocation;
	ClientListener(Socket client, PrintWriter out, BufferedReader in, HumanClient human){
		this.client = client;
		this.out = out;
		this.in = in;
		this.human = human;
		human.client = this;
		quadInfoLocation = 0;
		out.println("load texture");
		out.println("res/test.png");
		try{while(!in.ready());}
		catch(IOException e){}
		readIncomingData();
	}
	void readIncomingData(){
		try {
			if(in.ready()){
				String input = in.readLine();
				switch(input){
				case "screen info":
					human.setScreenSize(Location.parseLocation(in.readLine()));
					break;
				case "input":
					processInput(in.readLine());
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void processInput(String inputType) throws NumberFormatException, IOException{
		switch(inputType){
		case "click":
			int button = Integer.parseInt(in.readLine());		// Get the mouse button
			human.receiveMouseInput(button, in.readLine());		// Send the button and the type of key press
			break;
		case "key":
			int key = Integer.parseInt(in.readLine());			// First get the key
			human.receiveKeyInput(key, in.readLine()); 			// Then send both the key and the press type
			break;
		case "wheel":
			human.receiveMouseInput(4, in.readLine());	//Send information as a wheel reading
			break;
		case "mouse location":
			human.receiveMouseInput(0, in.readLine());
			break;
		default:
			System.out.println("unrecognized input type received from the client");
			break;
		}
	}
	void sendQuadInfo(String info){
		if(quadInfoLocation++==0)out.println("quad");
		out.println(info);
		if(quadInfoLocation == 18)quadInfoLocation = 0;
	}
}

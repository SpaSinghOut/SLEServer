package com.spartanlaboratories.engine.structure;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.spartanlaboratories.engine.util.Location;

class ClientListener{
	Socket client;
	public PrintWriter out;
	public BufferedReader in;
	HumanClient human;
	int quadInfoLocation;
	ClientListener(Socket client, PrintWriter out, BufferedReader in, HumanClient human){
		this.client = client;
		this.out = out;
		this.in = in;
		this.human = human;
		human.client = this;
		quadInfoLocation = 0;
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
					String inputType = in.readLine();
					if(inputType.equals("physical"))
						processPhysicalInput();
					else if(inputType.equals("virtual"))
						processVirtualInput();
					else 
						System.out.println("Unrecognized input type: ".concat(inputType));
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void processPhysicalInput() throws NumberFormatException, IOException{
		String type = in.readLine().toLowerCase();
		switch(type){
		case "click":
			int button = Integer.parseInt(in.readLine());		// Get the mouse button
			human.processMouseInput(button, in.readLine());		// Send the button and the type of key press
			break;
		case "key":
			int key = Integer.parseInt(in.readLine());			// First get the key
			human.processKeyInput(key, in.readLine()); 			// Then send both the key and the press type
			break;
		case "wheel":
			human.processMouseInput(4, in.readLine());	//Send information as a wheel reading
			break;
		case "mouse location":
			human.processMouseInput(0, in.readLine());
			break;
		default:
			System.out.println("unrecognized input type received from the client: ".concat(type));
			break;
		}
	}
	private void processVirtualInput() throws NumberFormatException, IOException{
		switch(in.readLine()) {
		case "button":
			int mouseButton = Integer.parseInt(in.readLine());
			String buttonAlternative = in.readLine();
			String[] buttonData = new String[Integer.parseInt(in.readLine())];
			for(int i = 0; i < buttonData.length; i++)
				buttonData[i] = in.readLine();
			human.processButtonClick(mouseButton, buttonAlternative, buttonData);
		}
	}
	void sendQuadInfo(String info){
		if(quadInfoLocation++==0)out.println("quad");
		out.println(info);
		if(quadInfoLocation == 18)quadInfoLocation = 0;
	}
}

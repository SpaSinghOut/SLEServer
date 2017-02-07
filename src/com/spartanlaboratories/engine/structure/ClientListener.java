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
	boolean debug = true;
	ClientListener(Socket client, PrintWriter out, BufferedReader in, HumanClient human){
		this.client = client;
		this.out = out;
		this.in = in;
		this.human = human;
		human.client = this;
		quadInfoLocation = 0;
		System.out.println("Waiting for Client to send data");
		try{while(!in.ready());}
		catch(IOException e){
			e.printStackTrace();
		}
		readIncomingData();
	}
	void readIncomingData(){
		try {
			if(in.ready()){
				System.out.println("reading client data");
				String input = read();
				switch(input){
				case "screen info":
					System.out.println("reading screen info");
					while(!in.ready());
					human.setScreenSize(Location.parseLocation(read()));
					break;
				case "input":
					processInput(read());
					System.out.println("reading input info");
					break;
				default:
					System.out.println("unrecognized input: " + input);
				}
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	private void processInput(String inputType) throws NumberFormatException, IOException{
		switch(inputType.toLowerCase()){
		case "click":
			int button = Integer.parseInt(read());		// Get the mouse button
			human.receiveMouseInput(button, read());		// Send the button and the type of key press
			break;
		case "key":
			int key = Integer.parseInt(read());			// First get the key
			human.receiveKeyInput(key, read()); 			// Then send both the key and the press type
			break;
		case "wheel":
			human.receiveMouseInput(4, read());			//Send information as a wheel reading
			break;
		case "mouse location":
			human.receiveMouseInput(0, read());
			break;
		case "button":
			String [] buttonData = new String[Integer.parseInt(read())];
			for(int i = 0; i < buttonData.length; i++)
				buttonData[i] = read();
			human.receiveButtonInput(buttonData);
			break;
		default:
			System.out.print("unrecognized input type received from the client: ");
			System.out.println(inputType);
			break;
		}
	}
	void sendQuadInfo(String info){
		if(quadInfoLocation++==0)out.println("quad");
		out.println(info);
		if(quadInfoLocation == 18)quadInfoLocation = 0;
	}
	private String read() throws IOException{
		String string;
		string = in.readLine();
		if(debug)
			System.out.println(string);
		return string;
	}
}

package com.spartanlaboratories.engine.game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.spartanlaboratories.engine.structure.SLEXMLException;

class MissileStats{
	boolean homing;
	boolean penetrating;
	double damage;
	int width;
	int height;
	double speed;
	String color;
	String name;
	private static final String exceptionMessage = "Error encountered while reading file: Missile.xml \r\n";
	MissileStats(String name){
		this.name = name;
		try{
			FileInputStream fw = new FileInputStream("Missiles.xml");
			XMLInputFactory xml = XMLInputFactory.newInstance();
			XMLStreamReader reader = xml.createXMLStreamReader(fw);
			while(reader.hasNext()){
				if(reader.isStartElement() && reader.getLocalName() == "Missile"){
					reader.next();
					if(reader.isCharacters())reader.next();
					String text = reader.getLocalName().toLowerCase();
					if(reader.isStartElement()&&text.equals("tname")){
						reader.next();
						if(!(reader.isStartElement()&&reader.isEndElement())){
							if(reader.getText().toLowerCase().equals(name.toLowerCase())){
								startParse(reader);
								return;
							}
						}
						else System.out.println("The <tName> tag should be followed by the ability's name");
					}
					else System.out.println("Ability declaration should be immediately followed by the <tName> tag");
				}
				reader.next();
			}
			System.out.println("Ability was not found in the Abilities.xml file");
		}catch(FileNotFoundException | XMLStreamException | SLEXMLException e){}
	}
	private void startParse(XMLStreamReader reader) throws SLEXMLException, XMLStreamException{
		while(reader.hasNext()){
			reader.next();
			if(reader.isStartElement()){
				System.out.println("reading the local name as " + reader.getLocalName().toLowerCase());
				switch(reader.getLocalName()){
				case "bHoming":
					reader.next();
					if(reader.getText().toLowerCase().equals("true"))homing = true;
					else if(reader.getText().toLowerCase().equals("false"))homing = false;
					else throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the homing value of: " + name);
					break;
				case "bPenetrating":
					reader.next();
					if(reader.getText().toLowerCase().equals("true"))penetrating = true;
					else if(reader.getText().toLowerCase().equals("false"))penetrating = false;
					else throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the penetrating value of: " + name);
					break;
				case "nDamage":
					reader.next();
					try{
						damage = Double.parseDouble(reader.getText());
					}catch(IllegalStateException | NullPointerException e){
						throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the damage value of: " + name +
								"\r\nMost likely the value is missing");
					}catch(NumberFormatException e){
						throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the damage value of: " + name +
								"\r\nMost likely the value is not of the appropriate type. A decimal is expected.");
					}
					break;
				case "nWidth":
					reader.next();
					try{
						width = Integer.parseInt(reader.getText());
					}catch(IllegalStateException | NullPointerException e){
						throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the width value of: " + name +
								"\r\nMost likely the value is missing");
					}catch(NumberFormatException e){
						throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the width value of: " + name +
								"\r\nMost likely the value is not of the appropriate type. An integer is expected.");
					}
					break;
				case "nHeight":
					reader.next();
					try{
						height = Integer.parseInt(reader.getText());
					}catch(IllegalStateException | NullPointerException e){
						throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the height value of: " + name +
								"\r\nMost likely the value is missing");
					}catch(NumberFormatException e){
						throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the height value of: " + name +
								"\r\nMost likely the value is not of the appropriate type. An integer is expected.");
					}
					break;
				case "nSpeed":
					reader.next();
					try{
						speed = Double.parseDouble(reader.getText());
					}catch(IllegalStateException | NullPointerException e){
						throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the speed value of: " + name +
								"\r\nMost likely the value is missing");
					}catch(NumberFormatException e){
						throw new SLEXMLException(exceptionMessage + "Error occured while trying to read the speed value of: " + name +
								"\r\nMost likely the value is not of the appropriate type. A decimal is expected.");
					}
					break;
				case "tColor":
					reader.next();
					color = reader.getText().toLowerCase();
					break;
				case "tName":
					reader.next();
					name = reader.getText();
					break;
					default: System.out.println("undefined field name: " + reader.getLocalName());
				}
			}
			else if(reader.isEndElement() && reader.getLocalName().equals("Ability")){
				System.out.println("Found end element: " + reader.getLocalName());
				break;
			}
		}
	}
}

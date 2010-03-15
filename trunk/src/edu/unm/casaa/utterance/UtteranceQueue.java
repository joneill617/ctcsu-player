/*
This source code file is part of the CASAA Treatment Coding System Utility
    Copyright (C) 2009  UNM CASAA

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.unm.casaa.utterance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.StringTokenizer;

import edu.unm.casaa.misc.MiscCode;
import edu.unm.casaa.misc.MiscDataItem;

/**
 * A double-ended queue that stores utterances in their recorded order.
 * @author Alex Manuel
 *
 */
public class UtteranceQueue extends LinkedList<Utterance> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Once the queue contains an element,m the iterator will be positioned
	// between the current element and the next element in the queue.
	// Thus, a call to previous returns the current element, and a call to next
	// returns the next element.
	//  Example:
	// [current] [next]
	//          ^
	ListIterator<Utterance> iter = null;

	/**
	 * Constructs a double-ended Queue to hold utterance objects in order.
	 */
	public UtteranceQueue(){
		if( this.isEmpty() ){
			iter = listIterator(0);
		}
		else{
			iter = super.listIterator(1);
		}
	}

	/**
	 * Returns the next open index at the end of the queue.
	 * This is used to set a new utterance's order value.
	 * @return the next open index
	 */
	public int getNextAvailableIndex(){
		return super.size();
	}

	/**
	 * Adds the specified utterance to the end of the queue.
	 * @param data
	 */
	public void addUtterance(Utterance data){
		//super.addLast(data);
		//iter = super.listIterator(iter.nextIndex()+1);
		iter.add(data);
	}

	/**
	 * Removes the last added utterance from the end of the list.
	 * @return the removed utterance
	 */
	public void removeLastAddedUtterance(){
		System.out.println("DEBUG: 1st queue size = " + this.size());
		if(iter.hasPrevious()){
			iter.previous();
			iter.remove();
		}
		//return super.removeLast();
		System.out.println("DEBUG: 2nd queue size = " + this.size());
	}

	/**
	 * Retrieves, but does not remove the last (or current) utterance.
	 * This is intended to be used to edit the current utterance.
	 * @return the current utterance being modified in the queue or null if no elements left
	 */
	public Utterance getCurrentUtterance(){
		if(iter.hasPrevious()){
			iter.previous();
			return iter.next();
		}
		else{
			return null;
		}
	}

	/**
	 * Retrieves, but does not remove the first utterance.
	 * @return the first utterance in the queue
	 */
	public Utterance getFirstUtterance(){
		Utterance firstUtterance = super.peek();
		iter = super.listIterator(1);
		return firstUtterance;
	}

	/**
	 * Retrieves, but does not remove, the utterance at the specified index.
	 * Enum = index + 1
	 * @param index
	 * @return the utterance at the specified index
	 */
	public Utterance getUtteranceAt(int index){
		iter = super.listIterator(index);
		return iter.next();
	}

	/**
	 * Retrieves, but does not remove, the next utterance based on the 
	 * list iterator's position.
	 * @return the utterance next in the iteration or null if no elements left in queue.
	 */
	public Utterance getNextUtterance(){
		if(iter.hasNext()){
			return iter.next();
		}
		else{
			return null;
		}
	}
	
	/**
	 * Retrieves, but does not remove, the next utterance based on the 
	 * list iterator's position.  DOES NOT ADVANCE THE LIST ITERATOR!
	 * @return the utterance next in the iteration or null if no elements left in queue.
	 */
	public Utterance getNextUtteranceNoAdvance(){
		if(iter.hasNext()){
			iter.next();
			return iter.previous();
		}
		else{
			return null;
		}
	}

	/**
	 * Retrieves, but does not remove, the previous utterance based on the 
	 * list iterator's position.
	 * @return the utterance next in the iteration or null if no elements left in queue.
	 */
	public Utterance getPreviousUtterance(){
		if(iter.hasPrevious()){
			System.out.println("DEBUG: 0th curr index = " + iter.nextIndex());
			iter.previous();
			System.out.println("DEBUG: 1st curr index = " + iter.nextIndex());
			if(iter.hasPrevious()){
				iter.previous();
				System.out.println("DEBUG: 2nd curr index = " + iter.nextIndex());
				return iter.next();
			}
		}
		return null;
	}

	/**
	 * Removes the code from the current utterance
	 */
	public void stripCodeFromCurrentUtterance(){
		Utterance curr = iter.previous();
		curr.setMiscCode(MiscCode.INVALID);
	}

	/**
	 * Sets the specified utterance into that index position in the queue.
	 * Returns the utterance previously set there.
	 * @param index
	 * @param newUtterance
	 * @return the old utterance from that index
	 */
	public Utterance editUtteranceAt(int index, Utterance newUtterance){
		iter = super.listIterator(index);
		return super.set(index, newUtterance);
	}

	public boolean isEmpty(){
		if( super.size() == 0 ){
			return true;
		}
		else{
			return false;
		}
	}

	public Utterance getLastCodedUtterance(){		
		iter = listIterator(super.size());
		while( iter.hasPrevious() && iter.previous().getMiscCode() == MiscCode.INVALID );
		return iter.next();
	}

	/**
	 * Removes and returns the first utterance in the queue, or
	 * null if the queue is empty.
	 * @return the first utterance in the queue or null if empty.
	 */
	public Utterance removeFirstUtterance(){
		return super.poll();
	}

	//a removal without replacement from the queue will require
	//updating the order value for all utterances occurring later
	//in the queue.  This could be achieved by using the
	//listIterator method, then passing over the remaining queue
	//and updating the order values to their new index value. 

	//writing out to a file
	public void writeToFile(File file, String filenameAudio){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter( new FileWriter(file, false)); //change to true if caching is implemented
			writer.println("Audio File:\t" + filenameAudio);
			while( ! this.isEmpty() ){
				writer.println(this.removeFirstUtterance().toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if( this.isEmpty() ){
				writer.close();
			}
		}
	}

	//loading from a file
	public String loadFromFile(File file){
		Scanner in = null;
		try {
			in = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if( ! in.hasNext() ){
			return ("ERROR: No Audio File Listed");
		}
		//eat the audio filename line
		String filenameAudio = in.nextLine();
		StringTokenizer headReader = new StringTokenizer(filenameAudio, "\t");
		headReader.nextToken(); //eat line heading "Audio Filename:"
		filenameAudio = headReader.nextToken();
		if( (filenameAudio.trim()).equalsIgnoreCase("") ){
			return ("ERROR: No Audio File Listed");
		}
		while( in.hasNextLine() ){
			//TODO: MISC is hard-coded
			//MISC format: int order, String startTime, String EndTime,
			//				int startBytes, int endBytes,
			//				int code, String codename
			String 			nextStr 	= in.nextLine();
			StringTokenizer st 			= new StringTokenizer(nextStr/*in.nextLine()*/, "\t");
			int 			lineSize 	= st.countTokens();  //5 = parsed only, 7 = coded
			int 			order 		= new Integer(st.nextToken()).intValue();

			//TODO: place a check for "null" in start and end fields 
			//		to report to the user.
			String 			start 		= st.nextToken();
			String 			end 		= st.nextToken();
			int 			stBytes 	= new Integer(st.nextToken()).intValue();
			int 			endBytes 	= new Integer(st.nextToken()).intValue();
			MiscDataItem 	item 		= new MiscDataItem(order, start, stBytes);

			item.setEndTime(end);
			item.setEndBytes(endBytes);
			if( lineSize == 7 ){
				item.setMiscCode(new Integer(st.nextToken()).intValue());
				st.nextToken(); //throw away the code string
			}
			this.addUtterance(item);
		}
		return filenameAudio;
	}

}

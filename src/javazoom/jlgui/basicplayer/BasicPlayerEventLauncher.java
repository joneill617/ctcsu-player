/*
 * BasicPlayerEventLauncher.
 * 
 * JavaZOOM : jlgui@javazoom.net
 *            http://www.javazoom.net
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */
package javazoom.jlgui.basicplayer;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class implements a threaded events launcher.
 */
public class BasicPlayerEventLauncher extends Thread
{
    private int code = -1;
    private int position = -1;
    private double value = 0.0;
    private Object description = null;
    // Parameterized to meet Java 1.6 specifications
    private Collection<Object> listeners = null;
    private Object source = null;

	//====================================================================
    // Changed for CASAA, 2010
    // Events are reported by launchers that run in separate threads, so it
    // is possible for listeners to receive events out of order.  Therefore,
    // each event is now assigned an index from the launcher, so listeners can
    // track event order.
    private static int s_index = 0;
    private int index = s_index++;

    /**
     * Contructor.
     * @param code
     * @param position
     * @param value
     * @param description
     * @param listeners
     * @param source
     */
    public BasicPlayerEventLauncher(int code, int position, double value, Object description, Collection<Object> listeners, Object source)
    {
        super();
        this.code = code;
        this.position = position;
        this.value = value;
        this.description = description;
        this.listeners = listeners;
        this.source = source;
    }

    public void run()
    {
        if (listeners != null)
        {
            Iterator<Object> it = listeners.iterator();
            while (it.hasNext())
            {
                BasicPlayerListener bpl = (BasicPlayerListener) it.next();
                BasicPlayerEvent event = new BasicPlayerEvent(source, code, position, value, description, index);
                bpl.stateUpdated(event);
            }
        }
    }
}

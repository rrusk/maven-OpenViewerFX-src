/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2016 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * GraphicsStates.java
 * ---------------
 */
package org.jpedal.parser.gs;

import org.jpedal.color.GenericColorSpace;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.TextState;
import org.jpedal.parser.ParserOptions;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Object;

public class GraphicsStates {

    /**flag to show if stack setup*/
    private boolean isStackInitialised;

    /**stack for graphics states*/
    private Vector_Object graphicsStateStack;

    /**stack for graphics states*/
    private Vector_Object strokeColorStateStack;

    /**stack for graphics states*/
    private Vector_Object nonstrokeColorStateStack;
    
    private Vector_Int nonstrokeColorValueStack,strokeColorValueStack;

    /**stack for graphics states*/
    private Vector_Object textStateStack;

    int depth;

    ParserOptions parserOptions=new ParserOptions();


    public GraphicsStates(final ParserOptions parserOptions) {
        this.parserOptions=parserOptions;
    }

    /**
     * put item in graphics stack
     */
    public void pushGraphicsState(final GraphicsState gs, final DynamicVectorRenderer current) {

        if(!isStackInitialised){
            isStackInitialised=true;

            graphicsStateStack = new Vector_Object(10);
            textStateStack = new Vector_Object(10);
            
            strokeColorStateStack= new Vector_Object(20);
            nonstrokeColorStateStack= new Vector_Object(20);
            
            nonstrokeColorValueStack= new Vector_Int(20);
            strokeColorValueStack= new Vector_Int(20);
            
            //clipStack=new Vector_Object(20);
        }

        depth++;

        //store
        graphicsStateStack.push(gs.deepCopy());

        //store clip
        //		Area currentClip=gs.getClippingShape();
        //		if(currentClip==null)
        //			clipStack.push(null);
        //		else{
        //			clipStack.push(currentClip.clone());
        //		}
        //store text state (technically part of gs)
        textStateStack.push(gs.getTextState().deepCopy());

        //save colorspaces
        nonstrokeColorStateStack.push(gs.nonstrokeColorSpace);
        strokeColorStateStack.push(gs.strokeColorSpace);
        
        //preserve colors
        final int strokeColorData = gs.strokeColorSpace.getColor().getRGB();
        final int nonStrokeColorData = gs.nonstrokeColorSpace.getColor().getRGB();

        strokeColorValueStack.push(strokeColorData);
        nonstrokeColorValueStack.push(nonStrokeColorData);

        //System.out.println(gs.nonstrokeColorSpace+" "+gs.strokeColorSpace);
        
        current.writeCustom(DynamicVectorRenderer.RESET_COLORSPACE,null);

    }

    /**
     * restore GraphicsState status from graphics stack
     */
    public GraphicsState restoreGraphicsState(final DynamicVectorRenderer current) {
		GraphicsState gs = null;
        if(!isStackInitialised){

            LogWriter.writeLog("No GraphicsState saved to retrieve");
            
            //reset to defaults
            gs=new GraphicsState();
            gs.setTextState(new TextState());

        }else if(depth>0){

            depth--;

            gs = (GraphicsState) graphicsStateStack.pull();
            gs.setTextState((TextState) textStateStack.pull());

            gs.strokeColorSpace=(GenericColorSpace) strokeColorStateStack.pull();
            gs.nonstrokeColorSpace=(GenericColorSpace) nonstrokeColorStateStack.pull();

            final int strokeColorData=strokeColorValueStack.pull();
            final int nonStrokeColorData= nonstrokeColorValueStack.pull();
            
            gs.resetColorSpaces(strokeColorData, nonStrokeColorData);
            
        }
        
        //save for later
        if (parserOptions.isRenderPage()){

            current.drawClip(gs,parserOptions.defaultClip,false) ;

            current.writeCustom(DynamicVectorRenderer.RESET_COLORSPACE,null);

            /*
             * align display
             */
            current.setGraphicsState(GraphicsState.FILL,gs.getAlpha(GraphicsState.FILL),gs.getBMValue());
            current.setGraphicsState(GraphicsState.STROKE,gs.getAlpha(GraphicsState.STROKE),gs.getBMValue());
        }

        return gs;
    }


    public int getDepth() {
        return depth;
    }

    public void correctDepth(final int currentDepth, final GraphicsState gs, final DynamicVectorRenderer current) {

        while(depth>currentDepth){
            restoreGraphicsState(current);
        }
    }
}

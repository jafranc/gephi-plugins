/*
 Copyright 2008-2011 Gephi
 Authors : 
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.plugins.layout.xyz;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.ui.propertyeditor.NodeColumnAllNumbersEditor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author JeF
 */
public class XYZLayout implements Layout {

    private final XYZLayoutBuilder builder;
    private GraphModel graphModel;
    private boolean done;
    //Params
    private boolean centered = true;
    private Column coordX;
    private Column coordY;
    private Column coordZ;
    private final int loopingDelay = 100;//ms
    private static int loopCounter = 0;
    
    private String projection = "OrthoX";
    public static String[] rows = {
        "OrthoX",
        "OrthoY",
        "OrthoZ"
    };


    public XYZLayout(XYZLayoutBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void resetPropertiesValues() {
        if (graphModel != null) {
            for (Column c : graphModel.getNodeTable()) {
                if (c.isNumber() && !c.isArray() && !c.isDynamic()) {
                    if (c.getId().equalsIgnoreCase("coordX")
                            || c.getId().equalsIgnoreCase("X")
                            || c.getTitle().equalsIgnoreCase("coordX")
                            || c.getTitle().equalsIgnoreCase("X")) {
                        coordX = c;
                    } else if (c.getId().equalsIgnoreCase("coordY")
                            || c.getId().equalsIgnoreCase("Y")
                            || c.getTitle().equalsIgnoreCase("coordY")
                            || c.getTitle().equalsIgnoreCase("Y")) {
                        coordY = c;
                    } else if (c.getId().equalsIgnoreCase("coordZ")
                            || c.getId().equalsIgnoreCase("Z")
                            || c.getTitle().equalsIgnoreCase("coordZ")
                            || c.getTitle().equalsIgnoreCase("Z")) {
                        coordZ = c;
                    }
                }
            }
        }
    }

    @Override
    public void initAlgo() {
	done = false;
    }

    @Override
    public void goAlgo() {
        double cX, cY, cZ;
        float nodeX, nodeY;
        float averageX = 0;
        float averageY = 0;
        Graph graph = graphModel.getGraphVisible();

        graph.readLock();

        List<Node> validNodes = new ArrayList<Node>();
        List<Node> invalidNodes = new ArrayList<Node>();

        // Set valid and non valid nodes:
        for (Node n : graph.getNodes()) {
            if (n.getAttribute(coordX) != null && n.getAttribute(coordY) != null) {
                validNodes.add(n);
            } else {
                invalidNodes.add(n);
            }
        }

	double theta = 2*Math.PI/10*loopCounter;	
        // Mercantor
        if (projection.equals("OrthoZ")) {
            
            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof XYZLayoutData)) {
                    n.setLayoutData(new XYZLayoutData());
                }
                cX = getNodeCoordX(graph, n);
                cY = getNodeCoordY(graph, n);
              
                nodeX = (float) cX;//(cX*Math.cos(theta) - cY*Math.sin(theta));
                nodeY = (float) cY;//(cX*Math.sin(theta) + cY*Math.cos(theta));

		
                averageX += nodeX;
                averageY += nodeY;
		
                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        } //
        else if (projection.equals("OrthoY")) {
            
            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof XYZLayoutData)) {
                    n.setLayoutData(new XYZLayoutData());
                }
                cX = getNodeCoordX(graph, n);
                cZ = getNodeCoordZ(graph, n);
               
                nodeX = (float) cX;//(cX*Math.cos(theta) - cZ*Math.sin(theta));
               	nodeY = (float) cZ;//(cX*Math.sin(theta) + cZ*Math.cos(theta));


                averageX += nodeX;
               	averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        }
        else if (projection.equals("OrthoX")) {
            
            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof XYZLayoutData)) {
                    n.setLayoutData(new XYZLayoutData());
                }

		cY = getNodeCoordY(graph, n);
                cZ = getNodeCoordZ(graph, n);
               
                nodeX = (float) cY;//(cY*Math.cos(theta) - cZ*Math.sin(theta));
                nodeY = (float) cZ;//(cY*Math.sin(theta) + cZ*Math.cos(theta));

	   
                averageX += nodeX;
                averageY += nodeY;
	
                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
	}

       
        //recenter the graph
        if (centered == true) {
            for (Node n : graph.getNodes()) {
                nodeX = n.x() - averageX;
                nodeY = n.y() - averageY;

                n.setX(nodeX);
                n.setY(nodeY);
            }
        }

        graph.readUnlock();

	if(++loopCounter>10)
		done = true;

	try{
		Thread.sleep(loopingDelay);
	}
	catch( InterruptedException ex )
	{
		Exceptions.printStackTrace(ex);
	}

       
    }

    private double getNodeCoordX(Graph graph, Node n) {
        Number lat = (Number) n.getAttribute(coordX, graph.getView());
        return lat.doubleValue();
    }

    private double getNodeCoordY(Graph graph, Node n) {
        Number lon = (Number) n.getAttribute(coordY, graph.getView());
        return lon.doubleValue();
    }

    private double getNodeCoordZ(Graph graph, Node n) {
        Number lon = (Number) n.getAttribute(coordZ, graph.getView());
        return lon.doubleValue();
    }
    
    @Override
    public void endAlgo() {
    }

    @Override
    public boolean canAlgo() {
        return !done && coordX != null && coordY != null && coordZ != null;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String XYZLAYOUT = "Xyz Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Column.class,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.coordX.name"),
                    XYZLAYOUT,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.coordX.desc"),
                    "getCoordX", "setCoordX", NodeColumnAllNumbersEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Column.class,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.coordY.name"),
                    XYZLAYOUT,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.coordY.desc"),
                    "getCoordY", "setCoordY", NodeColumnAllNumbersEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Column.class,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.coordZ.name"),
                    XYZLAYOUT,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.coordZ.desc"),
                    "getCoordZ", "setCoordZ", NodeColumnAllNumbersEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.projection.name"),
                    XYZLAYOUT,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.projection.desc"),
                    "getProjection", "setProjection", CustomComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.centered.name"),
                    XYZLAYOUT,
                    NbBundle.getMessage(XYZLayout.class, "XYZLayout.centered.desc"),
                    "isCentered", "setCentered"));
        
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public Boolean isCentered() {
        return centered;
    }

    public void setCentered(Boolean centered) {
        this.centered = centered;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
        resetPropertiesValues();
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    public Column getCoordX() {
        return coordX;
    }

    public void setCoordX(Column coordX) {
        this.coordX = coordX;
    }

    public Column getCoordY() {
        return coordY;
    }

    public void setCoordY(Column coordY) {
        this.coordY = coordY;
    }

    public Column getCoordZ() {
	return coordZ;
    }

    public void setCoordZ(Column coordZ) {
        this.coordZ = coordZ;
    }

    private static class XYZLayoutData implements LayoutData {

        //Data
        public double x = 0f;
        public double y = 0f;
	public double z = 0f;
    }
}

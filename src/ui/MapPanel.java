package ui;

import geography.Feature;
import geography.FeatureCollection;
import geography.Geometry;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.*;

import solutions.Settings;
import solutions.iDiscreteEventListener;

public class MapPanel extends JPanel implements MouseListener, MouseMotionListener, iDiscreteEventListener {
	public static double minx,maxx,miny,maxy;
	FeatureCollection featureCollection;
	boolean zooming = false;
	Rectangle selection = null;
	public JPanel seatsPanel;
	public static Stack<double[]> zoomStack = new Stack<double[]>();
	
	public static int FSAA = 1;
	public static Object RENDERING_INTERPOLATION = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
	
	public void invalidate() {
		super.invalidate();
		if( seatsPanel != null) {
			seatsPanel.invalidate();
		}
	}
	public void repaint() {
		super.repaint();
		if( seatsPanel != null) {
			seatsPanel.repaint();
		}
	}

	MapPanel() {
        // set a preferred size for the custom panel.
        setPreferredSize(new Dimension(200,200));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    public void paintComponent(Graphics graphics0) {
    	try {
    	Graphics2D graphics = (Graphics2D)graphics0;
    	Graphics2D g = null;
        super.paintComponent(graphics);
        if( Settings.num_maps_to_draw == 0) {
        	return;
        }
        FeatureCollection.shown_map = 0;
        Dimension d = this.getSize();
        if( Settings.num_maps_to_draw == 4) {
        	d.setSize(d.width/2, d.height/2);
        }
        for( int i = 0; i < Settings.num_maps_to_draw; i++) {
            FeatureCollection.shown_map = i;
            BufferedImage off_Image = null;
	        //graphics.setComposite(AlphaComposite.Src);
	        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RENDERING_INTERPOLATION);
	        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
	        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	
	        if( FSAA > 0) {
		        off_Image =
		        		  new BufferedImage(
		        				  (int) (d.getWidth()*FSAA), 
		        				  (int) (d.getHeight()*FSAA), 
		        		          BufferedImage.TYPE_INT_ARGB
		        		          );
		        g = off_Image.createGraphics();
	        } else {
	        	//g = graphics;
	        }
	        
	        //g.setComposite(AlphaComposite.Src);
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RENDERING_INTERPOLATION);
	        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	        
	        
	        double scalex = ((double)d.getWidth()*FSAA)/(maxx-minx);
	        double scaley = ((double)d.getHeight()*FSAA)/(maxy-miny);
	        Geometry.shiftx = minx;
	        Geometry.shifty = miny;
	        Geometry.scalex = scalex;
	        Geometry.scaley = scaley;
	        if( featureCollection != null) {
	        	featureCollection.draw(g);
	        }
	        if( zooming) {
	        	if( selection != null) {
	        		//Color c = new Color();
	        		g.setColor(new Color(128,128,128,128));
	        		g.fillRect(selection.x*FSAA, selection.y*FSAA, selection.width*FSAA, selection.height*FSAA);
	           		g.setColor(new Color(255,255,255,255));
	           		g.drawRect(selection.x*FSAA, selection.y*FSAA, selection.width*FSAA, selection.height*FSAA);
	        	}
	        }
	        //System.out.println(".");
	        if( FSAA > 0) {
	        	g.dispose();
	        }
	        //System.out.println("x");
	        if( FSAA == 4) {
	            //Dimension d = this.getSize();
	            //graphics.setComposite(AlphaComposite.Src);
	
	            BufferedImage off_Image2 =
	            		  new BufferedImage(
	            				  (int) (d.getWidth()*2), 
	            				  (int) (d.getHeight()*2), 
	            		          BufferedImage.TYPE_INT_ARGB
	            		          );
	            Graphics2D g2 = off_Image2.createGraphics();
	            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RENDERING_INTERPOLATION);
	            g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	
		        g2.drawImage(off_Image, 
		        		0, 0, (int)d.getWidth()*2, (int)d.getHeight()*2, 
		        		null);
		        
		        g2.dispose();
	
		        graphics.drawImage(off_Image2, 
		                (i%2)*d.width,
		                ((i-i%2)/2)*d.height,
		        		(int)d.getWidth(), (int)d.getHeight(), 
		        		null);
	
	        } else if( FSAA == 2 || true) {
		        graphics.drawImage(off_Image,
		                (i%2)*d.width,
		                ((i-i%2)/2)*d.height,
		        		(int)d.getWidth(), (int)d.getHeight(), 
		        		null);
	        }
        }
        //System.out.println("o");
    } catch (Exception ex) {
    	ex.printStackTrace();
    }
    }

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if( zooming) {
			selection = new Rectangle(arg0.getX(),arg0.getY(),0,0);
			
			return;
		}
		int x = arg0.getX();
		int y = arg0.getY();
		//System.out.println("mouse pressed "+x+" "+y);
		Feature f = getFeature(x,y);
		if( f == null) {
			//System.out.println("no feature");
			return;
		}
		f.toggleClicked();
		invalidate();
		repaint();
		// TODO Auto-generated method stub
		
	}
	Feature getFeature(int x, int y) {
		if( featureCollection == null || featureCollection.features == null) {
			return null;
		}
		for( Feature f : featureCollection.features) {
			for( Polygon p : f.geometry.polygons) {
				if( pnpoly(p,x*FSAA,y*FSAA)) {
					return f;
				}
			}
		}
		return null;
	}
	public boolean pnpoly(Polygon p, float testx, float testy) {
		int[] ivertx = p.xpoints;
		int[] iverty = p.ypoints;
		float[] vertx = new float[ivertx.length];
		for( int i = 0; i < vertx.length; i++) {
			vertx[i] = (float)ivertx[i];
		}
		float[] verty = new float[iverty.length];
		for( int i = 0; i < verty.length; i++) {
			verty[i] = (float)iverty[i];
		}
		
		int nvert = ivertx.length-1;
		  int i, j;
		  boolean c = false;
		  for (i = 0, j = nvert-1; i < nvert; j = i++) {
		    if ( ((verty[i]>testy) != (verty[j]>testy)) &&
			 (testx < (vertx[j]-vertx[i]) * (testy-verty[i]) / (verty[j]-verty[i]) + vertx[i]) )
		       c = !c;
		  }
		  return c;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if( zooming) {
			if( selection == null) {
				return;
			}
 			int w = arg0.getX()-selection.x;
 			int h = arg0.getY()-selection.y;
 			if( w < 0) {
 				selection.x = arg0.getX();
 				w = -w;
 			}
 			if( h < 0) {
 				selection.y = arg0.getY();
 				h = -h;
 			}
 			selection.width = w;
 			selection.height = h;
			zooming = false;
			zoomTo(selection);
			invalidate();
			repaint();
		}
	}
	public void zoomOut() {
		
		double[] dd = zoomStack.pop();
		if( dd == null) {
			return;
		}

        minx = dd[0];
        miny = dd[1];
        maxx = dd[2];
        maxy = dd[3];
        invalidate();
        repaint();
	}
	
	public void zoomTo(Rectangle r) {
        if( Settings.num_maps_to_draw == 0) {
        	return;
        }
        Dimension d = this.getSize();
        double scalex = ((double)d.getWidth())/(maxx-minx);
        double scaley = ((double)d.getHeight())/(maxy-miny);
		if( MapPanel.zoomStack.empty()) {
			//System.out.println(Geometry.scalex +" "+Geometry.scaley );
			double sign = scaley*scalex > 0 ? 1 : -1;
			if( Math.abs(FeatureCollection.xy*scaley) > Math.abs(scalex)) {
				scaley = sign * scalex/FeatureCollection.xy;
			} else if( Math.abs(FeatureCollection.xy*scaley) < Math.abs(scalex)) {
				scalex = sign * FeatureCollection.xy*scaley;
			}
		}

        if( Settings.num_maps_to_draw == 4) {
        	int x = (r.x+r.width);
        	int y = (r.y+r.height);
        	
        	x %= d.width/2;
        	y %= d.height/2;
        	x *= 2;
        	y *= 2;

        	r.x %= d.width/2;
        	r.y %= d.height/2;
        	r.x *= 2;
        	r.y *= 2;
        	
        	r.width = x-r.x;
        	r.height = y-r.y;
        }

/*
        if( MapPanel.zoomStack.empty()) {
        	scalex = Geometry.scalex;
        	scaley = Geometry.scaley;
        }*/
        double x0 = minx+r.x/scalex;
        double y0 = miny+r.y/scaley;
        double x1 = minx+(r.x+r.width)/scalex;
        double y1 = miny+(r.y+r.height)/scaley;
		zoomStack.push(new double[]{minx,miny,maxx,maxy});

        minx = x0;
        miny = y0;
        maxx = x1;
        maxy = y1;

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if( zooming) {
			if( selection == null) {
				return;
			}
 			int w = e.getX()-selection.x;
 			int h = e.getY()-selection.y;
 			selection.width = w;
 			selection.height = h;
			invalidate();
			repaint();
		}

		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventOccured() {
		invalidate();
		repaint();
	}
}

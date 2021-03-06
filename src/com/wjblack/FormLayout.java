package com.wjblack;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
   Implementation of a simple Form-type Layout.

   <p>
     This particular form layout is very simple to use, similar to BoxLayout
     in construction, and only a minor tweak to add() to add components with
     automatically generated (and laid out) with field names.  For example:</p>

   <p><img src="doc-files/FormLayout-1.gif" alt="Example FormLayout" /></p>

   <p>Generated by the (VERY) short program stanza:</p>
   
   <pre>
   [...control creation...]
   
   // The form panel
   JPanel form = new JPanel();
   FormLayout layout = new FormLayout(form);
   form.setLayout(layout);
   form.add("Name", name);
   form.add("Address", address);
   form.add("City/State/ZIP", csz);
   form.add("Interests", interests);
   form.add("Special Offers?", spam);
   form.add(FormLayout.BUTTONAREA, cancel);
   form.add(FormLayout.BUTTONAREA, save);
   
   [...assign to JFrame, etc...]
   </pre>

   <p>
     This layout is smart enough to generate a form whose fields are center
     aligned (i.e. their left edges are all aligned, and their labels' right
     edges are all aligned).  The buttons along the bottom are grouped on one
     line and aligned with the right edge of the container.  The idea is that,
     if you just want a bunch of text fields with a "Save" button or similar at
     the bottom, it's trivial to create.  This could be done with e.g.
     SpringLayout instead (much more complex) or another third-party tool like
     the stuff from JGoodies (too big of an external dependency, IMHO). This
     code is much simpler to use and much smaller (even with my verbose
     comments :-).</p>
   
   <p>
     New versions of this code are always available on
     <a href="https://github.com/wjblack/swingformlayout">GitHub</a>.
   </p>

   <p>
     Copyright (c) 2006-17 by <a href="mailto:bj@wjblack.com">BJ Black</a>
     and released unto an unsuspecting planet via the
     <a href="http://www.wtfpl.net/about/">WTFPL</a>.  See the COPYING file in
     the source for details.
   </p>

   <p>
     N.B. A previous version of this code (same author) was LGPL released in
     2006.  This code supercedes that project.
   </p>

   @author B.J. Black <bj@wjblack.com>
   @version 1.0 2006-12-17
*/
public class FormLayout implements LayoutManager2 {

	// the target container to be laid out.
	private Container target;

	// the fields in this form.
	private LinkedHashMap fields;

	// the buttons at the bottom of this form.
	private LinkedHashSet buttons;
	
	// spacing between components.
	private int spacing;

	/**
	 * Constant that specifies that the component to be added should be in
	 * the button area.
	 * 
	 * Use: container.add(FormLayout.BUTTONAREA, button);
	 */
	public static final String BUTTONAREA = "ButtonArea";

	/**
	 * Single constructor that requires that the area to be controlled is
	 * added.
	 * 
	 * @param target
	 *            the target Container.
	 */
	public FormLayout(Container target) {
		spacing = 5;
		this.target = target;
		fields = new LinkedHashMap();
		buttons = new LinkedHashSet();
	}

	// Methods required by LayoutManager2
	
	/**
	 * Add a component to the layout, either as a field or to the button
	 * area.
	 * 
	 * @param comp
	 *            the Component to add to the container.
	 * @param constraints
	 *            where/how to add this component to the container.
	 */
	public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints != null && constraints instanceof String) {
			addLayoutComponent((String) constraints, comp);
		}
	}

	/**
	 * Get the layout alignment for the container. Assumed to be always
	 * centered.
	 * 
	 * @param target
	 *            the Container we're looking at.
	 * @return 1/2 always.
	 */
	public float getLayoutAlignmentX(Container target) {
		return (float) 0.5;
	}

	/**
	 * Get the layout alignment for the container. Assumed to be always
	 * centered.
	 * 
	 * @param target
	 *            the Container we're looking at.
	 * @return 1/2 always.
	 */
	public float getLayoutAlignmentY(Container target) {
		return (float) 0.5;
	}

	/**
	 * Remove all cached info for this layout. We don't really cache any
	 * calculations (maybe a later version will). This is a no-op (for now).
	 * 
	 * @param target the Container we're looking at (in case the layout is
	 *        a singleton or something).
	 */
	public void invalidateLayout(Container target) {
		;
	}

	/**
	 * Get the maximum size that can be laid out. Just return the target
	 * size.
	 * 
	 * @param target
	 *            the Container we're looking at.
	 * @return the Container's max size (we'll try laying out anything).
	 */
	public Dimension maximumLayoutSize(Container target) {
		return target.getMaximumSize();
	}

	// Methods required by LayoutManager
	
	/**
	 * Add a component to this layout.
	 * 
	 * @param name
	 *            the field name for this item, or BUTTONAREA if it's
	 *            supposed to be a component in the button area.
	 * @param comp
	 *            the component we're adding.
	 */
	public void addLayoutComponent(String name, Component comp) {
		// It's either a button (add to button area)...
		if (name.equals(BUTTONAREA)) {
			buttons.add(comp);

			// ...or it's a normal field (make a label, too)...
		} else {
			if (!fields.containsKey(comp)) {
				JLabel lblField = new JLabel(name + ":");
				fields.put(lblField, comp);
				target.add(lblField);
			}
		}

		// Note that anything else is ignored.
	}

	/**
	 * Lay out the container with fields and buttons.
	 * 
	 * <p>
	 * This function first figures out how wide the widest label is (which
	 * determines where the center line is) and then how wide the buttons
	 * are (to right-align the button set). Then it starts at the first
	 * field and lays them out top-to-bottom, then the buttons.
	 * </p>
	 * 
	 * @param target
	 *            the container to lay out.
	 */
	public void layoutContainer(Container target) {
		// Figure out where the centerline will be.
		int labelWidth = calcFieldLabelWidth();
		// Figure out where the buttons' left edge (ButtonX) will be.
		int bx = target.getWidth() - calcButtonWidth() + spacing;
		// Starting Y location for the fields.
		int y = spacing;
		// Left edge of all fields (i.e. right after the centerline)
		int valx = 10 + labelWidth;
		// Width of all fields
		int valw = target.getWidth() - valx - spacing;

		// Lay out the fields
		Iterator i = fields.entrySet().iterator();
		while (i.hasNext()) {
			// Get the field labels (key) and field controls (val)
			Map.Entry ent = (Map.Entry) i.next();
			Component key = (Component) ent.getKey();
			Component val = (Component) ent.getValue();

			// Figure out the invariant bounds for the key (i.e.
			// everything but the Y, which might change if the
			// field is taller than the label)
			int keyx = spacing + labelWidth - key.getPreferredSize().width;
			int keyy = y;
			int keyw = key.getPreferredSize().width;
			int keyh = key.getPreferredSize().height;

			// Figure out the baseline bounds for the field. The X
			// is always the same (so it's been precalculated
			// above), as is the width.
			// The Y might change if the label is bigger than the
			// field.
			int valy = y;
			int valh = val.getPreferredSize().height;

			// Center the smaller component (either the key or val)
			// in the height of the taller component.  Add the
			// taller component's height to the base Y value
			// basically incrementing the count).
			if (keyh > valh) {
				valy = y + (keyh - valh) / 2;
				y += keyh + spacing;
			} else {
				keyy = y + (valh - keyh) / 2;
				y += valh + spacing;
			}

			// Set the key and val bounds.
			key.setBounds(keyx, keyy, keyw, keyh);
			val.setBounds(valx, valy, valw, valh);
		}

		// Layout the buttons. Start at bx (the first button's X) and
		// increment right.
		i = buttons.iterator();
		while (i.hasNext()) {
			Component comp = (Component) i.next();
			Dimension d = comp.getPreferredSize();
			comp.setBounds(bx, y, d.width, d.height);
			bx += comp.getWidth() + spacing;
		}
	}

	/**
	   Figure out how big this container needs to be to show everything
	   inside.
	
	   <p>
	     Similar to the layout process itself, we figure out the overall
	     height and width of all components using a combination of
	     calcTotalHeight, calcMaxWidth, etc.
	   </p>
	
	   @param parent the container we're calculating bounds for.
	   @return the minimum size of the container to show all items. 
	 */
	public Dimension minimumLayoutSize(Container parent) {
		
		// We try to figure out the width first.  It'll be the greater
		// of either:
		//    1.  The maximum label width + the maximum field width, or
		//    2.  The button panel width.
		int width = spacing + calcFieldLabelWidth() +
		            spacing + calcFieldDataWidth() +
		            spacing;
		int buttonWidth = calcButtonWidth();
		if (width < buttonWidth) {
			width = buttonWidth;
		}
	
		// Next we try to figure out the height.  It will be the sum of:
		//    1.  The height of all fields, and
		//    2.  The max height of the buttons.
		int height = calcMaxHeight(buttons.iterator()) +
		             calcTotalSetHeight(fields.entrySet().iterator());
		return new Dimension(width, height);
	}

	/**
	   Get the preferred layout size for this layout (the same as the
	   minimum)
	   
	   @param parent the Container we're looking at.
	   @return the preferred size.
	 */
	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}

	/**
	   Remove a field or button.  Note that, if a label is removed then its
	   associated data field is removed, and vice versa.
	   
	   @param comp the Component to be removed.
	 */
	public void removeLayoutComponent(Component comp) {
		if (buttons.contains(comp)) {
			buttons.remove(comp);
		} else if (fields.containsKey(comp)) {
			fields.remove(comp);
		} else {
			Iterator keys = fields.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				if (fields.get(key) == comp) {
					fields.remove(key);
				}
			}
		}
	}

	// Helper functions for above.
	
	/**
	 * Calculate the width of the widest field label.
	 * 
	 * @return the width of the widest label
	 */
	private int calcFieldLabelWidth() {
		Iterator i = fields.keySet().iterator();
		return calcMaxWidth(i);
	}

	/**
	 * Calculate the width of the widest field control.
	 * 
	 * @return the width of the widest control.
	 */
	private int calcFieldDataWidth() {
		Iterator i = fields.values().iterator();
		return calcMaxWidth(i);
	}

	/**
	   Figure out how much width the buttons will take up.
	   
	   @return width of the line of buttons.
	 */
	private int calcButtonWidth() {
		Iterator i = buttons.iterator();
		return calcTotalWidth(i);
	}

	/**
	 * Convenience function to calculate the total width of an iterated
	 * list of components.
	 * 
	 * @return the total width of all components in the iterator.
	 */
	private int calcTotalWidth(Iterator i) {
		int total = spacing;
		while (i.hasNext()) {
			Component comp = (Component) i.next();
			total += comp.getPreferredSize().width + spacing;
		}
		return total;
	}

	/**
	 * Convenience function to calculate the maximum width of an iterated
	 * list of components.
	 * 
	 * @return the max width of all components in the iterator.
	 */
	private int calcMaxWidth(Iterator i) {
		int width = 0;
		while (i.hasNext()) {
			Component comp = (Component) i.next();
			if (comp.getPreferredSize().width > width) {
				width = comp.getPreferredSize().width;
			}
		}
		return width;
	}

	/**
	 * Convenience function to calculate the maximum height of an iterated
	 * list of components.
	 * 
	 * @return the max height of all components in the iterator.
	 */
	private int calcMaxHeight(Iterator i) {
		int height = 0;
		while (i.hasNext()) {
			Component comp = (Component) i.next();
			if (comp.getPreferredSize().height > height) {
				height = comp.getPreferredSize().height;
			}
		}
		return height;
	}
	
	/**
	   Convenience function for totalling up the height of an iterator of
	   component sets.
	   
	   @param i an iterator of Component sets (key,val).
	   @return the total height (with item spacing) of all items.
	 */
	private int calcTotalSetHeight(Iterator i) {
		int total = spacing;
		while (i.hasNext()) {
			Map.Entry ent = (Map.Entry) i.next();
			Component key = (Component) ent.getKey();
			Component val = (Component) ent.getValue();
			int height = key.getPreferredSize().height;
			if( height < val.getPreferredSize().height ) {
				height = val.getPreferredSize().height;
			}
			total += height + spacing;
		}
		return total;
	}
}

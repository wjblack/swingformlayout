package com.wjblack;

import org.junit.*;
import static org.junit.Assert.*;
import com.wjblack.FormLayout;
import java.awt.*;
import javax.swing.*;

public class TestFormLayout {
	/* Simple test.  Basically lay out one k/v pair, check the dims.
	   Then add another k/v pair and make sure the width stayed the same
	   and the height grew. */
	@Test
	public void testCalcSetHeight() {
		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout(panel);
		panel.setLayout(layout);
		JTextField txt1 = new JTextField("Foobar");
		panel.add("Foobar", txt1);
		panel.revalidate();
		Dimension sizepre = layout.minimumLayoutSize(panel);
		JTextField txt2 = new JTextField("Foobar");
		panel.add("Foobar", txt2);
		panel.revalidate();
		Dimension sizepost = layout.minimumLayoutSize(panel);
		if(sizepre.width  != sizepost.width ||
		   sizepre.height >= sizepost.height ) {
			String s = String.format("%d, %d = %d, %d",
				sizepre.width, sizepre.height,
				sizepost.width, sizepost.height);
			fail(s);
		}
	}
}

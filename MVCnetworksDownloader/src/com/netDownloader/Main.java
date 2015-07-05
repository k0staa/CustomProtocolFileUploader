package com.netDownloader;

import com.netDownloader.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {
		final Model m = new Model();
		final View v = new View(m);
		final Controller c = new Controller(m, v);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLocationRelativeTo(null);
				frame.setMinimumSize(new Dimension(700,500));
				frame.setLayout(new BorderLayout());
				frame.add(v.getButtonComponentA(),BorderLayout.NORTH);
				frame.add(v.getListComponent(), BorderLayout.CENTER);
				frame.add(v.getButtonComponentB(),BorderLayout.SOUTH);

				frame.pack();
				frame.setVisible(true);
			}
			
			
		});
	}
}

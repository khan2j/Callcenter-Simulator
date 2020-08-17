/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package systemtools.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;

import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Dieses Panel enth�lt eine ein- und ausklappbare
 * Beschreibungsseite f�r ein {@link StatisticViewerText}-Element.
 * @author Alexander Herzog
 * @see StatisticViewerText
 * @version 1.2
 */
public class DescriptionViewer extends JPanel {
	private static final long serialVersionUID = 8594822323998433424L;

	private final Consumer<String> linkCallback;
	private final URL pageURL;
	private final JToolBar toolbar;
	private final JButton button;
	private final JTextPane textPane;
	private boolean descriptionVisible;

	/**
	 * Konstruktor der Klasse
	 * @param pageURL	Anzuzeigende Hilfeseite
	 * @param linkCallback	Callback an das Links (zur dialogbasierten Hilfe) �bermittelt werden (kann <code>null</code> sein)
	 */
	public DescriptionViewer(final URL pageURL, final Consumer<String> linkCallback) {
		super();
		this.pageURL=pageURL;
		this.linkCallback=linkCallback;
		setLayout(new BorderLayout());

		toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.add(button=new JButton());
		button.setText(StatisticsBasePanel.descriptionShow); /* Damit die gew�nschte Toolbar-H�he initial richtig berechnet werden kann. */
		button.setIcon(SimToolsImages.ARROW_UP.getIcon()); /* Damit die gew�nschte Toolbar-H�he initial richtig berechnet werden kann. */
		button.addActionListener(e->toggleDescription());
		add(toolbar,BorderLayout.NORTH);

		textPane=new JTextPane();
		textPane.setEditable(false);
		textPane.addHyperlinkListener(e->linkClicked(e));
		add(new JScrollPane(textPane),BorderLayout.CENTER);

		SwingUtilities.invokeLater(new InitRunnable());
	}

	private class InitRunnable implements Runnable {
		@Override
		public void run() {
			try {
				textPane.setPage(pageURL); /* Muss in SwingUtilities.invokeLater aufgerufen werden, sonst kann's bei der Report-Generierung Blockierungen geben. */
			} catch (IOException e1) {
				textPane.setText("Page "+pageURL.toString()+" not found.");
			}

			if (toolbar.getHeight()<20) {
				SwingUtilities.invokeLater(new InitRunnable());
				return;
			}
			descriptionVisible=true;
			toggleDescription();
		}
	}

	/**
	 * Benachrichtigt das Objekt, dass sich der Trenner im �bergeordneten
	 * {@link JSplitPane} verschoben wurde.
	 */
	public void splitterMovedInfo() {
		if (toolbar.getHeight()==0) return;
		if (!(getParent() instanceof JSplitPane)) return;

		final JSplitPane split=(JSplitPane)getParent();
		final int minSize=toolbar.getHeight()+split.getDividerSize();

		if (split.getDividerLocation()>split.getSize().height-minSize) {
			split.setDividerLocation(split.getSize().height-minSize);
		}

		descriptionVisible=(split.getDividerLocation()<split.getSize().height-minSize-1);
		updateButton();
	}

	private void updateButton() {
		if (descriptionVisible) {
			button.setText(StatisticsBasePanel.descriptionHide);
			button.setToolTipText(StatisticsBasePanel.descriptionHideHint);
			button.setIcon(SimToolsImages.ARROW_DOWN.getIcon());
		} else {
			button.setText(StatisticsBasePanel.descriptionShow);
			button.setToolTipText(StatisticsBasePanel.descriptionShowHint);
			button.setIcon(SimToolsImages.ARROW_UP.getIcon());
		}
	}

	private void toggleDescription() {
		descriptionVisible=!descriptionVisible;
		updateButton();

		if (getParent() instanceof JSplitPane) {
			final JSplitPane split=(JSplitPane)getParent();
			if (descriptionVisible) {
				split.setDividerLocation(2*split.getSize().height/3);
			} else {
				final int minSize=toolbar.getHeight()+split.getDividerSize();
				split.setDividerLocation(split.getSize().height-minSize);
			}
		}
	}

	private void linkClicked(final HyperlinkEvent e) {
		if (e.getEventType()==HyperlinkEvent.EventType.ENTERED) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return;
		}

		if (e.getEventType()==HyperlinkEvent.EventType.EXITED) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		}

		if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
			final String description=e.getDescription();
			if (description!=null && !description.trim().isEmpty()) {
				final String linkLower=description.toLowerCase();

				if (linkLower.startsWith("http://") || linkLower.startsWith("https://")) {
					if (!MsgBox.confirmOpenURL(DescriptionViewer.this,description)) return;
					try {Desktop.getDesktop().browse(new URL(description).toURI());} catch (IOException | URISyntaxException e1) {
						MsgBox.error(this,StatisticsBasePanel.internetErrorTitle,String.format(StatisticsBasePanel.internetErrorInfo,description));
					}
					return;
				}

				if (linkCallback!=null) linkCallback.accept(description);
			}
		}
	}

	@Override
	public Dimension getMaximumSize() {
		final Dimension size=super.getMaximumSize();
		if (descriptionVisible && getParent()!=null) {
			size.height=getParent().getHeight()/2;
		} else {
			size.height=toolbar.getPreferredSize().height;
		}
		return size;
	}

	@Override
	public Dimension getMinimumSize() {
		final Dimension size=super.getMinimumSize();
		size.height=toolbar.getPreferredSize().height;
		return size;
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension size=super.getPreferredSize();
		if (descriptionVisible && getParent()!=null) {
			size.height=getParent().getHeight()/3;
		} else {
			size.height=toolbar.getPreferredSize().height;
		}
		return size;
	}

	@Override
	public Dimension getSize() {
		final Dimension size=super.getSize();
		if (!descriptionVisible) {
			size.height=toolbar.getPreferredSize().height;
		}
		return size;
	}

	/**
	 * Liefert ein {@link JSplitPane}, welches oben den eigentlichen Inhalt
	 * und unten diesen {@link DescriptionViewer} enth�lt. Die Verkn�pfung
	 * zwischen Splitter und {@link DescriptionViewer} wird dabei auch bereits
	 * hergestellt.
	 * @param mainComponent	Komponenten f�r den oberen Bereich des Fensters
	 * @return	Splitter, der die Komponente und die Beschreibung enth�lt
	 */
	public JSplitPane getSplitPanel(final Component mainComponent) {
		final JSplitPane splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,mainComponent,this);
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(1);
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,e->splitterMovedInfo());
		splitPane.addComponentListener(new ComponentAdapter() {
			@Override public void componentResized(ComponentEvent e) {splitterMovedInfo();}
		});
		return splitPane;
	}
}
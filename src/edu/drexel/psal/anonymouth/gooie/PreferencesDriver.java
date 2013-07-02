package edu.drexel.psal.anonymouth.gooie;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.drexel.psal.anonymouth.gooie.DriverPreProcessTabDocuments.ExtFilter;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Exists to help organize the previously growing GeneralSettingsFrame into two main classes following the structure the rest of Anonymouth
 * Follows (NAMEwindow/frame/Pane and NAMEDriver), where the first sets up all the components, frames, and panels of the window, panel, or
 * tab and the second handles all the component listeners. 
 * @author Marc Barrowclift
 *
 */
public class PreferencesDriver {
	
	//various variables
	private static final String NAME = "( PreferencesDriver ) - ";
	private GUIMain main;
	private PreferencesWindow prefWin;
	private int prevFeatureValue;
	private int prevThreadValue;
	
	//Listeners
	private ActionListener classifierListener;
	private ActionListener featureListener;
	private ActionListener probSetListener;
	private ActionListener autoSaveListener;
	private ActionListener warnQuitListener;
	private ChangeListener maxFeaturesListener;
	private ChangeListener numOfThreadsListener;
	private ActionListener resetListener;
	private ActionListener translationsListener;
	private ChangeListener tabbedPaneListener;
	private ActionListener fontSizeListener;
	private KeyListener maxFeaturesBoxListener;
	private KeyListener numOfThreadsBoxListener;
	private ActionListener showWarningsListener;
	private ActionListener highlightElemsListener;
	private ActionListener versionAutoSaveListener;
	
	public PreferencesDriver(GUIMain main, PreferencesWindow prefWin) {
		this.main = main;
		this.prefWin = prefWin;
		
		prevFeatureValue = PropertiesUtil.getMaximumFeatures();
	}

	/**
	 * Initializes and adds all preferences window listeners
	 */
	public void initListeners() {
		fontSizeListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PropertiesUtil.setFontSize(prefWin.fontSizes.getSelectedItem().toString());
				main.normalFont = new Font("Ariel", Font.PLAIN, PropertiesUtil.getFontSize());
				main.getDocumentPane().setFont(main.normalFont);
			}
		};
		prefWin.fontSizes.addActionListener(fontSizeListener);
		
		tabbedPaneListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (prefWin.preferencesWindow == null)
					return;
				
				if (prefWin.tabbedPane.getSelectedIndex() == 0) {
					resize(prefWin.generalHeight);
					assertValues();
				} else if (prefWin.tabbedPane.getSelectedIndex() == 1) {
					resize(prefWin.defaultsHeight);
					assertValues();
				} else {
					resize(prefWin.advancedHeight);
				}
			}
		};
		prefWin.tabbedPane.addChangeListener(tabbedPaneListener);
		
		classifierListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PropertiesUtil.setClassifier(prefWin.classComboBox.getSelectedItem().toString());
			}
		};
		prefWin.classComboBox.addActionListener(classifierListener);
		
		featureListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PropertiesUtil.setFeature(prefWin.featComboBox.getSelectedItem().toString());
			}
		};
		prefWin.featComboBox.addActionListener(featureListener);
		
		probSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Logger.logln(NAME+"'Select' Problem Set button clicked on the Preferences window");

					int answer = 0;
					
					PropertiesUtil.load.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
					if (PropertiesUtil.getProbSet() != null) {
						String absPath = PropertiesUtil.propFile.getAbsolutePath();
						String problemSetDir = absPath.substring(0, absPath.indexOf("anonymouth_prop")-1) + "\\problem_sets\\";
						PropertiesUtil.load.setCurrentDirectory(new File(problemSetDir));
						PropertiesUtil.load.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
					}
					
					answer = PropertiesUtil.load.showDialog(main, "Load Problem Set");

					if (answer == JFileChooser.APPROVE_OPTION) {
						String path = PropertiesUtil.load.getSelectedFile().getAbsolutePath();
						PropertiesUtil.setProbSet(path);
						
						prefWin.probSetTextPane.setText(path);
					} else {
						Logger.logln(NAME+"Set default problem set canceled");
					}
				} catch (NullPointerException arg)
				{
					arg.printStackTrace();
				}
			}
		};
		prefWin.selectProbSet.addActionListener(probSetListener);
		
		autoSaveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {			
				if (prefWin.autoSave.isSelected()) {
					PropertiesUtil.setAutoSave(true);
					prefWin.warnQuit.setSelected(false);
					PropertiesUtil.setWarnQuit(false);
					prefWin.warnQuit.setEnabled(false);
					Logger.logln(NAME+"Auto-save checkbox checked");
				} else {
					PropertiesUtil.setAutoSave(false);
					prefWin.warnQuit.setEnabled(true);
					Logger.logln(NAME+"Auto-save checkbox unchecked");
				}
			}
		};
		prefWin.autoSave.addActionListener(autoSaveListener);
		
		warnQuitListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (prefWin.warnQuit.isSelected()) {
					PropertiesUtil.setWarnQuit(true);
					Logger.logln(NAME+"Warn on quit checkbox checked");
				} else {
					PropertiesUtil.setWarnQuit(false);
					Logger.logln(NAME+"Warn on quit checkbox unchecked");
				}
			}
		};
		prefWin.warnQuit.addActionListener(warnQuitListener);
		
		translationsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"Translations checkbox clicked");
				
				if (prefWin.translations.isSelected()) {
					if (GUIMain.processed)
						main.resetTranslator.setEnabled(true);
					PropertiesUtil.setDoTranslations(true);
					
					if (BackendInterface.processed) {
						int answer = JOptionPane.showOptionDialog(null,
								"Being translating now?",
								"Begin Translations",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null, null, null);
						
						if (answer == JOptionPane.YES_OPTION) {
							GUIMain.GUITranslator.load(DriverEditor.taggedDoc.getTaggedSentences());
							DriverTranslationsTab.showTranslations(DriverEditor.taggedDoc.getSentenceNumber(DriverEditor.sentToTranslate));
						}
					} else {
						main.notTranslated.setText("Please process your document to recieve translation suggestions.");
						main.translationsHolderPanel.add(main.notTranslated, "");
					}
				} else {
					main.resetTranslator.setEnabled(false);
					GUIMain.GUITranslator.reset();
					PropertiesUtil.setDoTranslations(false);
					main.notTranslated.setText("You have turned translations off.");
					main.translationsHolderPanel.add(main.notTranslated, "");
				}
			}
		};
		prefWin.translations.addActionListener(translationsListener);
		
		maxFeaturesListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PropertiesUtil.setMaximumFeatures(prefWin.maxFeaturesSlider.getValue());
				prefWin.maxFeaturesBox.setText(Integer.toString(PropertiesUtil.getMaximumFeatures()));
				prevFeatureValue = prefWin.maxFeaturesSlider.getValue();
			}	
		};
		prefWin.maxFeaturesSlider.addChangeListener(maxFeaturesListener);
		
		numOfThreadsListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PropertiesUtil.setThreadCount(prefWin.numOfThreadsSlider.getValue());
				prefWin.numOfThreadsBox.setText(Integer.toString(PropertiesUtil.getThreadCount()));
				prevThreadValue = prefWin.numOfThreadsSlider.getValue();
			}
		};
		prefWin.numOfThreadsSlider.addChangeListener(numOfThreadsListener);
		
		resetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"resetAll button clicked");
				
				int answer = 0;
				
				answer = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to resetAll all preferences?\nThis will override your changes.",
						"resetAll Preferences",
						JOptionPane.WARNING_MESSAGE,
						JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (answer == 0) {
					try {
						Logger.logln(NAME+"resetAll progressing...");
						//resets everything in the prop file to their default values
						PropertiesUtil.reset();
						
						//updating the GUI to reflect the changes
						//general
						prefWin.warnQuit.setSelected(PropertiesUtil.getWarnQuit());
						prefWin.autoSave.setSelected(PropertiesUtil.getAutoSave());
						prefWin.fontSizes.setSelectedItem(PropertiesUtil.getFontSize());
						prefWin.translations.setSelected(PropertiesUtil.getDoTranslations());
						prefWin.showWarnings.setSelected(PropertiesUtil.getWarnAll());
						prefWin.highlightElems.setSelected(PropertiesUtil.getAutoHighlight());
						
						//defaults
						prefWin.probSetTextPane.setText(PropertiesUtil.getProbSet());
						prefWin.featComboBox.setSelectedItem(PropertiesUtil.getFeature());
						prefWin.classComboBox.setSelectedItem(PropertiesUtil.getClassifier());
						
						//advanced
						prefWin.numOfThreadsSlider.setValue(PropertiesUtil.getThreadCount());
						prefWin.maxFeaturesSlider.setValue(PropertiesUtil.getMaximumFeatures());
						prefWin.versionAutoSave.setSelected(PropertiesUtil.getVersionAutoSave());
						Logger.logln(NAME+"resetAll complete");
					} catch (Exception e) {
						Logger.logln(NAME+"Error occurred during resetAll");
					}
				} else {
					Logger.logln(NAME+"User cancelled resetAll");
				}
			}
		};
		prefWin.resetAll.addActionListener(resetListener);
		
		maxFeaturesBoxListener = new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				int number = -1;
				try {
					if (prefWin.maxFeaturesBox.getText().equals("")) {
						prefWin.maxFeaturesBox.setText("");
					} else {
						number = Integer.parseInt(prefWin.maxFeaturesBox.getText());
						
						if (number > 1000) {
							prefWin.maxFeaturesBox.setText(Integer.toString(prevFeatureValue));
							number = prevFeatureValue;
						} else {
							prefWin.maxFeaturesBox.setText(Integer.toString(number));
						}
					}
				} catch (Exception e1) {
					prefWin.maxFeaturesBox.setText(Integer.toString(prevFeatureValue));
				}
				
				if (number != -1) {
					prevFeatureValue = number;
				}
				
				if (prevFeatureValue >= 200) {
					PropertiesUtil.setMaximumFeatures(prevFeatureValue);
					prefWin.maxFeaturesSlider.setValue(prevFeatureValue);
				}
			}
		};
		prefWin.maxFeaturesBox.addKeyListener(maxFeaturesBoxListener);
		
		numOfThreadsBoxListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				int number = -1;
				try {
					if (prefWin.numOfThreadsBox.getText().equals("")) {
						prefWin.numOfThreadsBox.setText("");
					} else {
						number = Integer.parseInt(prefWin.numOfThreadsBox.getText());
						
						if (number > 8) {
							prefWin.numOfThreadsBox.setText(Integer.toString(prevThreadValue));
							number = prevThreadValue;
						} else {
							prefWin.numOfThreadsBox.setText(Integer.toString(number));
						}
					}
				} catch (Exception e1) {
					prefWin.numOfThreadsBox.setText(Integer.toString(prevThreadValue));
				}
				
				if (number != -1) {
					prevThreadValue = number;
				}
				
				if (prevThreadValue >= 1) {
					PropertiesUtil.setMaximumFeatures(prevThreadValue);
					prefWin.numOfThreadsSlider.setValue(prevThreadValue);
				}
			}
		};
		prefWin.numOfThreadsBox.addKeyListener(numOfThreadsBoxListener);
		
		showWarningsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (prefWin.showWarnings.isSelected()) {
					PropertiesUtil.setWarnAll(true);
					Logger.logln(NAME+"Show all warnings checkbox checked");
				} else {
					PropertiesUtil.setWarnAll(false);
					Logger.logln(NAME+"Show all warnings checkbox unchecked");
				}
			}
		};
		prefWin.showWarnings.addActionListener(showWarningsListener);
		
		highlightElemsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (prefWin.highlightElems.isSelected()) {
					PropertiesUtil.setAutoHighlight(true);
					DriverEditor.highlightWordsToRemove(main, DriverEditor.selectedSentIndexRange[0], DriverEditor.selectedSentIndexRange[1]);
					Logger.logln(NAME+"Auto highlights checkbox checked");
				} else {
					PropertiesUtil.setAutoHighlight(false);
					DriverEditor.removeHighlightWordsToRemove(main);
					Logger.logln(NAME+"Auto highlights checkbox unchecked");
				}
			}
		};
		prefWin.highlightElems.addActionListener(highlightElemsListener);
		
		versionAutoSaveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (prefWin.versionAutoSave.isSelected()) {
					PropertiesUtil.setVersionAutoSave(true);
					ThePresident.SHOULD_KEEP_AUTO_SAVED_ANONYMIZED_DOCS = true;
					Logger.logln(NAME+"Version auto save checkbox checked");
				} else {
					PropertiesUtil.setVersionAutoSave(false);
					ThePresident.SHOULD_KEEP_AUTO_SAVED_ANONYMIZED_DOCS = false;
					Logger.logln(NAME+"Version auto save checkbox unchecked");
				}
			}
		};
		prefWin.versionAutoSave.addActionListener(versionAutoSaveListener);
	}
	
	/**
	 * Provides a nice animation when resizing the window
	 * @param newSize - The new height of the window
	 */
	public void resize(int newSize) {
		int curHeight = prefWin.getHeight();
		
		//If the new height is larger we need to grow the window height
		if (newSize >= curHeight) {
			for (int h = curHeight; h <= newSize; h+=10) {
				prefWin.setSize(new Dimension(500, h));
			}
		} else { //If the new height is smaller we need to shrink the window height
			for (int h = curHeight; h >= newSize; h-=10) {
				prefWin.setSize(new Dimension(500, h));
			}
		}

		prefWin.setSize(new Dimension(500, newSize)); //This is to ensure that our height is the desired height.
	}
	
	/**
	 * Used to assert that the values entered in the text fields for the advanced tab are valid, and fixing them if not.
	 */
	protected void assertValues() {
		int feat = PropertiesUtil.getMaximumFeatures();
		int thread = PropertiesUtil.getThreadCount();
		if (feat < 200 || feat > 1000)
			PropertiesUtil.setMaximumFeatures(PropertiesUtil.defaultFeatures);
		if (thread < 1 || thread > 8)
			PropertiesUtil.setThreadCount(PropertiesUtil.defaultThreads);
	}
}
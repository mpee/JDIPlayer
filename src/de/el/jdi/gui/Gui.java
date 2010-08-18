package de.el.jdi.gui;

import de.el.jdi.DIChannel;
import de.el.jdi.DIChannelFactory;
import de.el.jdi.DIPlayer;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swt.ProgressDialogWorker;
import swt.TextProgressBar;

/**
 *
 * @author PEH
 */
public class Gui {

	public Gui() {
		LOG.debug("constructing Gui");
		display = Display.getDefault();
		shell = new Shell(display, SWT.TITLE | SWT.CLOSE | SWT.MIN);
		shell.setLayout(new FillLayout());
		shell.setImage(new Image(display, Gui.class.getResourceAsStream("resources/ico.png")));
		player = new DIPlayer();
		initGui();
		
	}
	private static final Logger LOG = LoggerFactory.getLogger(Gui.class);
	private static Display display;
	private static Shell shell;
	private Composite composite;
	private Table channelTable;
	private Text channelSearch;
	private static DIPlayer player;
	private Button playButton, stopButton;
	private Image stopImage, playImage;
	private TextProgressBar bufferProgress;
	private List<DIChannel> channelList;

	private void initGui() {
		stopImage = new Image(display, Gui.class.getResourceAsStream("resources/stop.png"));
		playImage = new Image(display, Gui.class.getResourceAsStream("resources/play.png"));
		composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FormLayout());

		channelSearch = new Text(composite, SWT.BORDER);
		playButton = new Button(composite, SWT.PUSH);
		stopButton = new Button(composite, SWT.PUSH);
		channelTable = new Table(composite, SWT.BORDER | SWT.SINGLE);
		bufferProgress = new TextProgressBar(composite, SWT.NONE);

		FormData f = new FormData();
		f.top = new FormAttachment(0, 8);
		f.left = new FormAttachment(0, 5);
		f.right = new FormAttachment(playButton, -5);
		channelSearch.setLayoutData(f);
		channelSearch.addModifyListener(new ChannelSearchModiyListener());
		channelSearch.addKeyListener(new ChannelSearchKeyListener());



		f = new FormData();
		f.right = new FormAttachment(stopButton, -5);
		f.top = new FormAttachment(0, 5);
		playButton.setLayoutData(f);
		playButton.setImage(playImage);
		playButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				playSelectedChannel();
			}
		});


		f = new FormData();
		f.top = new FormAttachment(0, 5);
		f.right = new FormAttachment(100, -5);
		stopButton.setLayoutData(f);
		stopButton.setImage(stopImage);
		stopButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				player.stop();
			}
		});


		f = new FormData();
		f.top = new FormAttachment(channelTable, 5);
		f.left = new FormAttachment(0, 5);
		f.right = new FormAttachment(100, -5);
		f.bottom = new FormAttachment(100, -5);
		bufferProgress.setLayoutData(f);


		f = new FormData();
		f.top = new FormAttachment(channelSearch, 5);
		f.left = new FormAttachment(0, 5);
		f.right = new FormAttachment(100, -5);
		f.bottom = new FormAttachment(bufferProgress, -5);
		f.height = 200;
		channelTable.setLayoutData(f);
		channelTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				playSelectedChannel();
			}
		});

		bufferProgress.setShowText(true);
		bufferProgress.setText("Buffer status: %% %");


	}

	public void open() {
		shell.pack();
		shell.open();
		new ChannelListGetterThread(shell, SWT.INDETERMINATE, "Updating channellist").execute();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		player.stop();
	}

	public void addChannels() {
		for (DIChannel d : channelList) {
			TableItem t = new TableItem(channelTable, SWT.NONE);
			t.setText(d.getChannelName());
			t.setData(d);
		}
	}

	private void playSelectedChannel() {
		if (channelTable.getSelectionCount() == 0) {
			return;
		}
		TableItem t = channelTable.getSelection()[0];
		DIChannel channel = (DIChannel) t.getData();
		LOG.debug("{}", channel.getChannelName());
		try {
			player.stop();
			player.setChannel(new URL(channel.getChannelURL()));
			player.play();
		} catch (Exception ex) {
			LOG.debug("", ex);
		}
	}

	private class ChannelSearchKeyListener extends KeyAdapter {

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == 13 || e.keyCode == SWT.KEYPAD_CR) {
				playSelectedChannel();
				channelTable.setFocus();
			}
		}
	}

	private class ChannelSearchModiyListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			for (TableItem t : channelTable.getItems()) {
				DIChannel di = (DIChannel) t.getData();
				if (di.getChannelName().toLowerCase().startsWith(channelSearch.getText().toLowerCase())) {
					channelSearch.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
					channelTable.setSelection(t);
					return;
				}
			}
			channelSearch.setBackground(new Color(display, 250, 150, 150));
		}
	}

	private class ChannelListGetterThread extends ProgressDialogWorker<List<DIChannel>>{

		public ChannelListGetterThread(Shell shell, int flags, String title) {
			super(shell, flags, title);
		}


		@Override
		protected List<DIChannel> doInBackground() throws Exception {
			List<DIChannel> toReturn;
			File channelFile = new File(System.getProperty("user.home") + File.separator + "channels.dat");
			try{
				showDialog();
				if (DIChannelFactory.isChannelListOutdated(channelFile)) {
					publish("This could take a minute");
					toReturn = DIChannelFactory.updateChannelListFile(channelFile);
				} else {
					publish("Importing chanellist.");
					toReturn = DIChannelFactory.getChannelListFromFile(channelFile);
				}	
			} finally{
				disposeDialog();
			}
			return toReturn;
		}

		@Override
		protected void done() {
			try{
				channelList = get();
				addChannels();
			} catch(ExecutionException ex){

			} catch(InterruptedException ex){
				
			}
		}
		
	}

}

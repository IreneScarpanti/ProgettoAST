package com.scarpanti.app.playqueue.view.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.scarpanti.app.playqueue.controller.GenreController;
import com.scarpanti.app.playqueue.controller.PlayQueueController;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.view.PlayQueueView;

public class PlayQueueSwingView extends JFrame implements PlayQueueView {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private JList<Song> playQueueList;
	private DefaultListModel<Song> playQueueListModel;
	private JButton playNextButton;
	private JButton removeSelectedButton;
	private JButton cleanQueueButton;
	private JScrollPane playQueueScrollPane;

	private JList<Genre> genreList;
	private DefaultListModel<Genre> genreListModel;
	private JScrollPane genreScrollPane;

	private JList<Song> songList;
	private DefaultListModel<Song> songListModel;
	private JButton addToQueueButton;
	private JScrollPane songScrollPane;

	private transient Map<Song, Long> songToQueueIdMap;

	public PlayQueueSwingView() {
		songToQueueIdMap = new HashMap<>();
		setTitle("PlayQueue");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 400, 400, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JLabel lblPlayQueue = new JLabel("Play Queue");
		GridBagConstraints gbc_lblPlayQueue = new GridBagConstraints();
		gbc_lblPlayQueue.insets = new Insets(0, 0, 5, 5);
		gbc_lblPlayQueue.gridx = 0;
		gbc_lblPlayQueue.gridy = 0;
		contentPane.add(lblPlayQueue, gbc_lblPlayQueue);

		playQueueListModel = new DefaultListModel<>();
		playQueueList = new JList<>(playQueueListModel);
		playQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playQueueList.setName("playQueueList");

		playQueueScrollPane = new JScrollPane(playQueueList);
		GridBagConstraints gbc_playQueueScrollPane = new GridBagConstraints();
		gbc_playQueueScrollPane.gridheight = 3;
		gbc_playQueueScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_playQueueScrollPane.fill = GridBagConstraints.BOTH;
		gbc_playQueueScrollPane.gridx = 0;
		gbc_playQueueScrollPane.gridy = 1;
		contentPane.add(playQueueScrollPane, gbc_playQueueScrollPane);

		JPanel playQueueButtonPanel = new JPanel();
		GridBagConstraints gbc_playQueueButtonPanel = new GridBagConstraints();
		gbc_playQueueButtonPanel.insets = new Insets(0, 0, 0, 5);
		gbc_playQueueButtonPanel.fill = GridBagConstraints.BOTH;
		gbc_playQueueButtonPanel.gridx = 0;
		gbc_playQueueButtonPanel.gridy = 4;
		contentPane.add(playQueueButtonPanel, gbc_playQueueButtonPanel);

		GridBagLayout gbl_playQueueButtonPanel = new GridBagLayout();
		gbl_playQueueButtonPanel.columnWidths = new int[] { 195, 195, 0 };
		gbl_playQueueButtonPanel.rowHeights = new int[] { 27, 0 };
		gbl_playQueueButtonPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_playQueueButtonPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		playQueueButtonPanel.setLayout(gbl_playQueueButtonPanel);

		playNextButton = new JButton("Play Next");
		playNextButton.setName("playNextButton");
		playNextButton.setEnabled(false);
		GridBagConstraints gbc_playNextButton = new GridBagConstraints();
		gbc_playNextButton.fill = GridBagConstraints.BOTH;
		gbc_playNextButton.insets = new Insets(0, 0, 0, 5);
		gbc_playNextButton.gridx = 0;
		gbc_playNextButton.gridy = 0;
		playQueueButtonPanel.add(playNextButton, gbc_playNextButton);

		removeSelectedButton = new JButton("Remove Selected");
		removeSelectedButton.setName("removeSelectedButton");
		removeSelectedButton.setEnabled(false);
		GridBagConstraints gbc_removeSelectedButton = new GridBagConstraints();
		gbc_removeSelectedButton.fill = GridBagConstraints.BOTH;
		gbc_removeSelectedButton.gridx = 1;
		gbc_removeSelectedButton.gridy = 0;
		playQueueButtonPanel.add(removeSelectedButton, gbc_removeSelectedButton);

		cleanQueueButton = new JButton("Clean");
		cleanQueueButton.setName("cleanQueueButton");
		cleanQueueButton.setEnabled(false);

		GridBagConstraints gbc_cleanQueueButton = new GridBagConstraints();
		gbc_cleanQueueButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_cleanQueueButton.gridx = 1;
		gbc_cleanQueueButton.gridy = 1;
		gbc_cleanQueueButton.gridwidth = 1;
		gbc_cleanQueueButton.insets = new Insets(40, 0, 0, 0);
		playQueueButtonPanel.add(cleanQueueButton, gbc_cleanQueueButton);

		JLabel lblMusicLibrary = new JLabel("Music Library");
		GridBagConstraints gbc_lblMusicLibrary = new GridBagConstraints();
		gbc_lblMusicLibrary.insets = new Insets(0, 0, 5, 0);
		gbc_lblMusicLibrary.gridx = 1;
		gbc_lblMusicLibrary.gridy = 0;
		contentPane.add(lblMusicLibrary, gbc_lblMusicLibrary);

		JLabel lblGenres = new JLabel("Genres:");
		GridBagConstraints gbc_lblGenres = new GridBagConstraints();
		gbc_lblGenres.insets = new Insets(0, 0, 5, 0);
		gbc_lblGenres.gridx = 1;
		gbc_lblGenres.gridy = 1;
		contentPane.add(lblGenres, gbc_lblGenres);

		genreListModel = new DefaultListModel<>();
		genreList = new JList<>(genreListModel);
		genreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		genreList.setName("genreList");

		genreScrollPane = new JScrollPane(genreList);
		GridBagConstraints gbc_genreScrollPane = new GridBagConstraints();
		gbc_genreScrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_genreScrollPane.fill = GridBagConstraints.BOTH;
		gbc_genreScrollPane.gridx = 1;
		gbc_genreScrollPane.gridy = 2;
		contentPane.add(genreScrollPane, gbc_genreScrollPane);

		JLabel lblSongs = new JLabel("Songs:");
		GridBagConstraints gbc_lblSongs = new GridBagConstraints();
		gbc_lblSongs.insets = new Insets(0, 0, 5, 0);
		gbc_lblSongs.gridx = 1;
		gbc_lblSongs.gridy = 3;
		contentPane.add(lblSongs, gbc_lblSongs);

		JPanel songPanel = new JPanel(new BorderLayout());
		GridBagConstraints gbc_songPanel = new GridBagConstraints();
		gbc_songPanel.fill = GridBagConstraints.BOTH;
		gbc_songPanel.gridx = 1;
		gbc_songPanel.gridy = 4;
		contentPane.add(songPanel, gbc_songPanel);

		songListModel = new DefaultListModel<>();
		songList = new JList<>(songListModel);
		songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		songList.setName("songList");

		songScrollPane = new JScrollPane(songList);
		songPanel.add(songScrollPane, BorderLayout.CENTER);

		addToQueueButton = new JButton("Add to Queue");
		addToQueueButton.setName("addToPlayQueueButton");
		addToQueueButton.setEnabled(false);
		songPanel.add(addToQueueButton, BorderLayout.SOUTH);
	}

	public void setGenreController(GenreController genreController) {
		genreController.loadGenres();
	}

	public void setPlayQueueController(PlayQueueController playQueueController) {
		playQueueController.getPlayQueue();
	}

	@Override
	public void showGenres(List<Genre> genres) {
		genreListModel.clear();
		genres.forEach(genreListModel::addElement);

	}

	@Override
	public void showSongs(List<Song> songs) {
		songListModel.clear();
		songs.forEach(songListModel::addElement);

	}

	@Override
	public void showQueue(Map<Long, Song> songs) {
		playQueueListModel.clear();
		songToQueueIdMap.clear();
		for (Map.Entry<Long, Song> entry : songs.entrySet()) {
			Song song = entry.getValue();
			Long queueId = entry.getKey();
			playQueueListModel.addElement(song);
			songToQueueIdMap.put(song, queueId);
		}

	}
}
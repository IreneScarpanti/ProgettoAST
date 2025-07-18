package com.scarpanti.app.playqueue.view.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.scarpanti.app.playqueue.controller.GenreController;
import com.scarpanti.app.playqueue.controller.PlayQueueController;
import com.scarpanti.app.playqueue.controller.SongController;
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
	private JScrollPane playQueueScrollPane;

	private JList<Genre> genreList;
	private DefaultListModel<Genre> genreListModel;
	private JScrollPane genreScrollPane;

	private JList<Song> songList;
	private DefaultListModel<Song> songListModel;
	private JButton addToPlayQueueButton;
	private JScrollPane songScrollPane;

	private transient GenreController genreController;
	private transient SongController songController;
	private transient PlayQueueController playQueueController;

	public PlayQueueSwingView() {
		setTitle("PlayQueue");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		playNextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (playQueueController != null) {
					playQueueController.onPlayNext();
				}
			}
		});
		GridBagConstraints gbc_playNextButton = new GridBagConstraints();
		gbc_playNextButton.fill = GridBagConstraints.BOTH;
		gbc_playNextButton.insets = new Insets(0, 0, 0, 5);
		gbc_playNextButton.gridx = 0;
		gbc_playNextButton.gridy = 0;
		playQueueButtonPanel.add(playNextButton, gbc_playNextButton);

		removeSelectedButton = new JButton("Remove Selected");
		removeSelectedButton.setName("removeSelectedButton");
		removeSelectedButton.setEnabled(false);
		removeSelectedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (playQueueController != null) {
					Song selectedSong = playQueueList.getSelectedValue();
					if (selectedSong != null) {
						playQueueController.onSongRemoved(selectedSong);
					}
				}
			}
		});
		GridBagConstraints gbc_removeSelectedButton = new GridBagConstraints();
		gbc_removeSelectedButton.fill = GridBagConstraints.BOTH;
		gbc_removeSelectedButton.gridx = 1;
		gbc_removeSelectedButton.gridy = 0;
		playQueueButtonPanel.add(removeSelectedButton, gbc_removeSelectedButton);
		playQueueList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					boolean queueSongSelected = playQueueList.getSelectedValue() != null;
					removeSelectedButton.setEnabled(queueSongSelected);
				}
			}
		});

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
		genreList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Genre selectedGenre = genreList.getSelectedValue();
					if (selectedGenre != null && songController != null) {
						songController.onGenreSelected(selectedGenre);
					}
				}
			}
		});

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

		addToPlayQueueButton = new JButton("Add to Queue");
		addToPlayQueueButton.setName("addToPlayQueueButton");
		addToPlayQueueButton.setEnabled(false);
		addToPlayQueueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (playQueueController != null) {
					Song selectedSong = songList.getSelectedValue();
					if (selectedSong != null) {
						playQueueController.onSongSelected(selectedSong);
					}
				}
			}
		});
		songPanel.add(addToPlayQueueButton, BorderLayout.SOUTH);
		songList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					boolean songSelected = songList.getSelectedValue() != null;
					addToPlayQueueButton.setEnabled(songSelected);
				}
			}
		});
	}

	public GenreController getGenreController() {
		return genreController;
	}

	public void setGenreController(GenreController genreController) {
		this.genreController = genreController;
		if (genreController != null) {
			genreController.loadGenres();
		}
	}

	public SongController getSongController() {
		return songController;
	}

	public void setSongController(SongController songController) {
		this.songController = songController;
	}

	public PlayQueueController getPlayQueueController() {
		return playQueueController;
	}

	public void setPlayQueueController(PlayQueueController playQueueController) {
		this.playQueueController = playQueueController;
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
		boolean songSelected = songList.getSelectedValue() != null;
		addToPlayQueueButton.setEnabled(songSelected);
	}

	@Override
	public void showQueue(List<Song> songs) {
		playQueueListModel.clear();
		songs.forEach(playQueueListModel::addElement);
		boolean queueNotEmpty = playQueueListModel.getSize() > 0;
		playNextButton.setEnabled(queueNotEmpty);
		boolean queueSongSelected = playQueueList.getSelectedValue() != null;
		removeSelectedButton.setEnabled(queueSongSelected);
	}
}
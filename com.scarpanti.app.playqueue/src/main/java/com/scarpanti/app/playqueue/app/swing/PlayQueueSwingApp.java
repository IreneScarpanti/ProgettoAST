package com.scarpanti.app.playqueue.app.swing;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.scarpanti.app.playqueue.controller.GenreController;
import com.scarpanti.app.playqueue.controller.PlayQueueController;
import com.scarpanti.app.playqueue.controller.SongController;
import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.transaction.jpa.JpaTransactionManager;
import com.scarpanti.app.playqueue.view.swing.PlayQueueSwingView;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class PlayQueueSwingApp implements Callable<Void> {

	@Option(names = { "--db-host" }, description = "Database host address")
	private String dbHost = "localhost";

	@Option(names = { "--db-port" }, description = "Database port")
	private int dbPort = 3307;

	@Option(names = { "--db-name" }, description = "Database name")
	private String databaseName = "playqueue";

	@Option(names = { "--db-user" }, description = "Database user")
	private String dbUser = "root";

	@Option(names = { "--db-password" }, description = "Database password")
	private String dbPassword = "password";

	@Option(names = { "--populate-db" }, description = "Populate database with sample data")
	private boolean populateDB = false;

	private EntityManagerFactory entityManagerFactory;
	private JpaTransactionManager transactionManager;

	public static void main(String[] args) {
		new CommandLine(new PlayQueueSwingApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				Map<String, String> properties = new HashMap<>();
				properties.put("javax.persistence.jdbc.url",
						"jdbc:mysql://" + dbHost + ":" + dbPort + "/" + databaseName);
				properties.put("javax.persistence.jdbc.user", dbUser);
				properties.put("javax.persistence.jdbc.password", dbPassword);
				properties.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
				properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
				properties.put("hibernate.hbm2ddl.auto", "update");
				properties.put("hibernate.show_sql", "false");

				entityManagerFactory = Persistence.createEntityManagerFactory("playqueue", properties);
				transactionManager = new JpaTransactionManager(entityManagerFactory);

				if (populateDB) {
					populateDBIfEmpty();
				}

				PlayQueueSwingView playQueueView = new PlayQueueSwingView();
				GenreController genreController = new GenreController(transactionManager, playQueueView);
				SongController songController = new SongController(transactionManager, playQueueView);
				PlayQueueController playQueueController = new PlayQueueController(transactionManager, playQueueView);

				playQueueView.setGenreController(genreController);
				playQueueView.setSongController(songController);
				playQueueView.setPlayQueueController(playQueueController);

				playQueueView.setVisible(true);

			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception in PlayQueue Application", e);
			}
		});
		return null;
	}

	private void populateDBIfEmpty() {
		Logger logger = Logger.getLogger(getClass().getName());
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			Long genreCount = em.createQuery("SELECT COUNT(g) FROM GenreEntity g", Long.class).getSingleResult();

			if (genreCount == 0) {
				logger.info("Database is empty, populating with sample data...");
				em.getTransaction().begin();

				GenreEntity rockEntity = new GenreEntity("Rock", "Rock music");
				GenreEntity jazzEntity = new GenreEntity("Jazz", "Jazz music");
				GenreEntity popEntity = new GenreEntity("Pop", "Pop music");
				em.persist(rockEntity);
				em.persist(jazzEntity);
				em.persist(popEntity);

				em.persist(new SongEntity("Bohemian Rhapsody", "Queen", 354, rockEntity));
				em.persist(new SongEntity("Stairway To Heaven", "Led Zeppelin", 482, rockEntity));
				em.persist(new SongEntity("Take Five", "Dave Brubeck", 324, jazzEntity));
				em.persist(new SongEntity("Billie Jean", "Michael Jackson", 294, popEntity));

				em.getTransaction().commit();
				logger.info("Database populated successfully!");
			} else {
				logger.info("Database already populated");
			}
		} finally {
			em.close();
		}
	}
}
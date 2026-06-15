package game.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.model.Entity;
import engine.model.Game;
import engine.model.Grid;
import engine.model.Model;
import engine.view.View;
import game.model.Boss;
import game.model.Ghost;
import game.model.Gum;
import game.model.Obstacle;
import game.model.PacMan;
import game.view.BossAvatar;
import game.view.GhostSpriteAvatar;
import game.view.GumAvatar;
import game.view.ObstacleAvatar;
import game.view.PacManSpriteAvatar;

/*
 * LevelBuilder construit le contenu d'un niveau.
 *
 * Responsabilités :
 * - lire la carte textuelle ;
 * - créer les entités spécifiques au jeu ;
 * - leur associer un avatar ;
 * - demander au Model de les installer.
 *
 * LevelBuilder ne modifie jamais directement :
 * - les cellules de la grille ;
 * - la position interne d'une entité ;
 * - le centre ISU d'une entité ;
 * - le Bounding d'une entité.
 *
 * C'est le Model qui conserve la cohérence entre :
 * - la position Grid ;
 * - le centre ISU ;
 * - le Bounding ;
 * - les cellules occupées.
 */
public final class LevelBuilder {

	/*
	 * Signification des caractères :
	 *
	 * # : mur . : gum P : PacMan G : fantôme B : boss espace : case vide
	 */
	private static final String[] MAP = { "###################", "#........#........#", "#.###.##.#.##.###.#",
			"#.................#", "#.###.#.###.#.###.#", "#.....#..#..#.....#", "#####.##.#.##.#####",
			"    #.#.....#.#    ", "#####.#.###.#.#####", "     G..G...       ", "#####.#.###.#.#####",
			"    #.#.....#.#    ", "#####.#.###.#.#####", "#........#........#", "#.###.##.#.##.###.#",
			"#...#....P....#...#", "###.#.#.###.#.#.###", "#.....#..#..#.....#", "#.#######.#######.#",
			"#.................#", "###################" };

	/*
	 * Les couleurs sont attribuées dans cet ordre lorsque plusieurs caractères G
	 * sont présents.
	 */
	private static final String[] GHOST_COLORS = { "red", "pink", "cyan", "orange" };

	private final Game game;
	private final Model model;
	private final Grid grid;
	private final View view;

	private PacMan pacman;
	private Boss boss;

	private final List<Ghost> ghosts;
	private final List<Gum> gums;
	private final List<Obstacle> walls;

	private boolean built;

	public LevelBuilder(Game game, View view) {
		assert game != null;
		assert view != null;
		assert game.model() != null;
		assert game.getGrid() != null;

		/*
		 * La vue et le LevelBuilder doivent travailler sur le même Model.
		 */
		assert view.model() == game.model() : "View and LevelBuilder must use the same Model";

		this.game = game;
		this.model = game.model();
		this.grid = game.getGrid();
		this.view = view;

		this.pacman = null;
		this.boss = null;

		this.ghosts = new ArrayList<Ghost>();
		this.gums = new ArrayList<Gum>();
		this.walls = new ArrayList<Obstacle>();

		this.built = false;
	}

	/*
	 * Construit toutes les entités décrites par MAP.
	 *
	 * Cette méthode ne peut être appelée qu'une fois sur une même instance de
	 * LevelBuilder.
	 */
	public void build() {
		if (this.built) {
			throw new IllegalStateException("This level has already been built");
		}

		checkMapDimensions();

		for (int y = 0; y < MAP.length; y++) {
			String line = MAP[y];

			for (int x = 0; x < line.length(); x++) {
				char symbol = line.charAt(x);

				buildCell(symbol, x, y);
			}
		}

		checkRequiredEntities();

		this.built = true;
	}

	/*
	 * Interprète un caractère de la carte.
	 */
	private void buildCell(char symbol, int x, int y) {
		switch (symbol) {
		case '#':
			addWall(x, y);
			break;

		case '.':
			addGum(x, y);
			break;

		case 'P':
			addPacMan(x, y);
			break;

		case 'G':
			addGhost(x, y);
			break;

		case 'B':
			addBoss(x, y);
			break;

		case ' ':
			/*
			 * Une case vide ne crée aucune entité.
			 */
			break;

		default:
			throw new IllegalArgumentException(
					"Unknown map symbol '" + symbol + "' at position (" + x + ", " + y + ")");
		}
	}

	/*
	 * Vérifie que la carte textuelle possède exactement les mêmes dimensions que la
	 * Grid.
	 */
	private void checkMapDimensions() {
		int expectedWidth = this.grid.width();
		int expectedHeight = this.grid.height();

		if (MAP.length != expectedHeight) {
			throw new IllegalStateException("Map height is " + MAP.length + " but Grid height is " + expectedHeight);
		}

		for (int y = 0; y < MAP.length; y++) {
			String line = MAP[y];

			if (line.length() != expectedWidth) {
				throw new IllegalStateException(
						"Map line " + y + " has width " + line.length() + " but Grid width is " + expectedWidth);
			}
		}
	}

	/*
	 * Vérifie que le niveau contient les entités nécessaires pour lancer le jeu.
	 */
	private void checkRequiredEntities() {
		if (this.pacman == null) {
			throw new IllegalStateException("The level must contain one PacMan");
		}

		if (this.ghosts.isEmpty()) {
			throw new IllegalStateException("The level must contain at least one Ghost");
		}
	}

	/*
	 * Crée une position appartenant à la Grid du jeu.
	 *
	 * Le constructeur de Grid.Position applique la normalisation prévue par la
	 * géométrie du monde.
	 */
	private Grid.Position positionAt(int x, int y) {
		return this.grid.new Position(x, y);
	}

	/*
	 * Installe une entité dans le Model.
	 *
	 * LevelBuilder ne fait pas :
	 *
	 * entity.setPosition(...) entity.deploy() cell.add(entity)
	 *
	 * Toutes ces responsabilités appartiennent au Model.
	 */
	private <E extends Entity> E install(E entity, int x, int y) {
		assert entity != null;

		Grid.Position initialPosition = positionAt(x, y);

		this.model.add(entity, initialPosition);

		return entity;
	}

	/*
	 * Ajoute un mur.
	 */
	private void addWall(int x, int y) {
		Obstacle wall = new Obstacle(this.model, "wall-" + x + "-" + y);

		wall.setAvatar(new ObstacleAvatar(this.view, wall));

		install(wall, x, y);

		this.walls.add(wall);
	}

	/*
	 * Ajoute une Gum.
	 */
	private void addGum(int x, int y) {
		Gum gum = new Gum(this.model, "gum-" + x + "-" + y);

		gum.setAvatar(new GumAvatar(this.view, gum));

		install(gum, x, y);

		this.gums.add(gum);
	}

	/*
	 * Ajoute PacMan.
	 *
	 * Un niveau ne doit contenir qu'un seul caractère P.
	 */
	private void addPacMan(int x, int y) {
		if (this.pacman != null) {
			throw new IllegalStateException("The level contains more than one PacMan");
		}

		PacMan entity = new PacMan(this.model, "pacman");

		entity.setAvatar(new PacManSpriteAvatar(this.view, entity));

		this.pacman = install(entity, x, y);
	}

	/*
	 * Ajoute un Ghost.
	 *
	 * Les couleurs sont choisies successivement : red, pink, cyan, orange, puis on
	 * recommence.
	 */
	private void addGhost(int x, int y) {
		int ghostNumber = this.ghosts.size();

		String color = GHOST_COLORS[ghostNumber % GHOST_COLORS.length];

		Ghost ghost = new Ghost(this.model, "ghost-" + ghostNumber);

		ghost.setAvatar(new GhostSpriteAvatar(this.view, ghost, color));

		install(ghost, x, y);

		this.ghosts.add(ghost);
	}

	/*
	 * Ajoute un Boss.
	 *
	 * Le support du caractère B est déjà prêt, même si la carte actuelle ne
	 * contient pas encore de Boss.
	 */
	private void addBoss(int x, int y) {
		if (this.boss != null) {
			throw new IllegalStateException("The level contains more than one Boss");
		}

		Boss entity = new Boss(this.model, "boss");

		entity.setAvatar(new BossAvatar(this.view, entity));

		this.boss = install(entity, x, y);
	}

	public boolean built() {
		return this.built;
	}

	public PacMan pacman() {
		return this.pacman;
	}

	/*
	 * Compatibilité avec ton Main actuel : retourne le premier Ghost du niveau.
	 */
	public Ghost ghost() {
		if (this.ghosts.isEmpty()) {
			return null;
		}

		return this.ghosts.get(0);
	}

	public List<Ghost> ghosts() {
		return Collections.unmodifiableList(this.ghosts);
	}

	public List<Gum> gums() {
		return Collections.unmodifiableList(this.gums);
	}

	public List<Obstacle> walls() {
		return Collections.unmodifiableList(this.walls);
	}

	public Boss boss() {
		return this.boss;
	}
}
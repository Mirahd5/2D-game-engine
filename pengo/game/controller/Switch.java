package game.controller;

import java.util.ArrayList;
import java.util.List;

import engine.brain.Brain;
import engine.brain.PlayerBot;
import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Model;
import engine.view.Ticker;
import gal.ast.Automaton;
import game.gal.CurrentPlayer;
import game.gal.GalBot;
import game.model.Friend;
import game.model.Pengo;
import game.model.Player;

public final class Switch {

	private final Brain brain;
	private final Model model;
	private final Ticker ticker;
	private final Pengo pengo;
	private final List<Friend> friends;
	private final Automaton randomAutomaton;
	private final CurrentPlayer currentPlayer;

	private Entity controlledEntity;
	private PlayerBot playerBot;

	public Switch(Brain brain, Model model, Ticker ticker, Pengo pengo, List<Friend> friends, Automaton randomAutomaton,
			CurrentPlayer currentPlayer) {
		assert brain != null;
		assert model != null;
		assert ticker != null;
		assert pengo != null;
		assert friends != null;
		assert randomAutomaton != null;
		assert currentPlayer != null;

		this.brain = brain;
		this.model = model;
		this.ticker = ticker;
		this.pengo = pengo;
		this.friends = new ArrayList<Friend>(friends);
		this.randomAutomaton = randomAutomaton;
		this.currentPlayer = currentPlayer;

		this.controlledEntity = null;
		this.playerBot = null;

		control(this.pengo);
	}

	public Entity controlledEntity() {
		return this.controlledEntity;
	}

	public PlayerBot playerBot() {
		return this.playerBot;
	}

	public void switchControl() {
		updateControlledEntity();

		if (this.pengo.isGameOver() || !this.pengo.alive()) {
			return;
		}

		Entity next = nextControlledEntity();

		if (next == null || next == this.controlledEntity) {
			return;
		}

		Entity previous = this.controlledEntity;

		makeAutomatic(previous);
		control(next);
	}

	public void updateControlledEntity() {
		if (this.controlledEntity != null && this.controlledEntity.alive() && this.controlledEntity.installed()) {
			return;
		}

		Entity previousControlledEntity = this.controlledEntity;

		if (previousControlledEntity instanceof Friend) {
			Friend friend = (Friend) previousControlledEntity;
			friend.setControlled(false);
		}

		if (this.pengo.alive() && this.pengo.installed() && this.pengo.nLives() > 0) {
			control(this.pengo);
			return;
		}

		this.controlledEntity = null;
		this.playerBot = null;
	}

	private Entity nextControlledEntity() {
		if (this.controlledEntity instanceof Pengo) {
			return firstAvailableFriend();
		}

		return this.pengo;
	}

	private Friend firstAvailableFriend() {
		for (Friend friend : this.friends) {
			if (friend.alive() && friend.installed() && friend.nLives() > 0) {
				return friend;
			}
		}

		return null;
	}

	private void makeAutomatic(Entity entity) {
		if (entity == null || !entity.alive() || !entity.installed()) {
			return;
		}

		this.brain.removeBotsOf(entity);

		if (entity instanceof Friend) {
			Friend friend = (Friend) entity;
			friend.setControlled(false);
		}

		if (!(entity instanceof Player)) {
			return;
		}

		BasicStunt autoStunt = new BasicStunt(this.model, entity);
		entity.setStunt(autoStunt);

		GalBot autoBot = new GalBot(this.brain, autoStunt, entity, this.randomAutomaton, this.currentPlayer);

		this.brain.add(autoBot);
	}

	private void control(Entity entity) {
		if (entity == null || !entity.alive() || !entity.installed()) {
			return;
		}

		this.brain.removeBotsOf(entity);

		BasicStunt playerStunt = new BasicStunt(this.model, entity);
		entity.setStunt(playerStunt);

		if (entity instanceof Friend) {
			Friend friend = (Friend) entity;
			friend.setControlled(true);
		}

		this.playerBot = new PlayerBot(this.brain, playerStunt);
		this.brain.add(this.playerBot);

		this.controlledEntity = entity;
		this.currentPlayer.setCurrentPlayer(entity);

		this.model.viewPort().centerOn(entity);
		this.ticker.setFollowedEntity(entity);
	}
}
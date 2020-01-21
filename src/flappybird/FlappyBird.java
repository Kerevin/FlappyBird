package flappybird;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Dang Khoa
 */
public class FlappyBird extends Application {

	public static Label scoreLabel;
	private static int score;
	private static boolean running = false;
	private static double time = 0;
	public static Pane root = new Pane();
	public static StackPane stack = new StackPane();
	private static final int width = 600, height = 600;
	Bird bird = new Bird(50, height / 2, 88, 64);
	static int frame = 1;
	static Button startBtn = new Button("Tap to play");

	private void createPipes() {
		int gap = 260;

		// Top pipe
		int h1 = (new Random().nextInt(120) * (-1)) + 260;
		Sprite pipe = new Sprite(width - 10, 0, 80, h1, "pipe", Color.GREENYELLOW);
		pipe.setFill(new ImagePattern(new Image(FlappyBird.class.getResource("top.png").toString())));
		root.getChildren().add(pipe);

		// Bottom pipe
		pipe = new Sprite(width - 10, h1 + gap, 80, height - h1 - gap + 50, "pipe", Color.GREENYELLOW);
		pipe.setFill(new ImagePattern(new Image(FlappyBird.class.getResource("bottom.png").toString())));
		root.getChildren().add(pipe);

	}

	private void setScoreLabel() {
		score = 0;
		scoreLabel = new Label("Score: 0");
		scoreLabel.setFont(new Font("Arial", 20));
		scoreLabel.setTextFill(Color.WHITESMOKE);

	}

	private Parent createScene() {
		root.setPrefSize(width, height);

		startBtn.setPrefSize(100, 50);
		startBtn.setEffect(new DropShadow(5, Color.YELLOWGREEN));
		startBtn.setStyle("-fx-font: 14 arial; -fx-base: #F4FF81;");
		createPipes();
		setScoreLabel();

		root.getChildren().addAll(bird);
		root.setBackground(Background.EMPTY);
		stack.setBackground(Background.EMPTY);

		stack.getChildren().addAll(root, scoreLabel, startBtn);
		stack.setAlignment(scoreLabel, Pos.TOP_LEFT);
		stack.setAlignment(startBtn, Pos.CENTER);

		AnimationTimer timer = new AnimationTimer() {

			@Override
			public void handle(long now) {
				if (running == true) {
					update();
				}
				if (bird.dead) {
					bird.fall();

				}
			}
		};
		timer.start();
		return stack;
	}

	public List<Sprite> sprites() {
		return root.getChildren().stream().filter(n -> n instanceof Sprite).map(n -> (Sprite) n).collect(Collectors.toList());
	}

	public void update() {
		time += 0.01;
		sprites().forEach((Sprite s) -> {
			switch (s.getType()) {
				case "bird":
					bird.fall();
					// When bird falls 
					if (bird.getTranslateY() > height) {
						running = false;
						time = 0;
						bird.die();
					}
					break;

				case "pipe":
					s.moveLeft();
					// Pipes disapear
					if (s.getTranslateX() < -80) {
						s.dead = true;

						// Bird goes through pipes
					} else if (s.getTranslateX() == bird.getTranslateX()) {
						score += 1;
						bird.scoreUp();
						scoreLabel.setText("Score: " + (score / 2));
					}
					// Bird meets pipes

					if (s.getBoundsInParent().intersects(bird.getBoundsInParent())) {
						running = false;
						time = 0;
						bird.die();
					}
					break;
			}
		});

		// Set frame for bird's animation
		bird.setFill(bird.getAnimation().get(frame / 5));

		frame = (frame + 1) % 10;
		if (time > 1.2) {
			createPipes();
			time = 0;
		}

		// remove pipes which have gone far away
		root.getChildren()
				.removeIf(n -> {
					if (n instanceof Sprite) {
						Sprite s = (Sprite) n;
						return (s.dead && s.getType().equals("pipe"));
					}
					return false;
				}
				);
	}

	public void resetGame() {
		score = 0;
		scoreLabel.setText("Score: 0");
		root.getChildren().removeIf(p -> {
			if (p instanceof Sprite) {
				Sprite n = (Sprite) p;
				return (n.getType().equals("pipe"));
			}
			return false;
		});

		bird.setTranslateY(height / 2);
		bird.dead = false;

		bird.setRotate(0);
		createPipes();
		stack.getChildren().add(startBtn);
	}

	@Override
	public void start(Stage stage) throws InterruptedException {

		stage.setTitle("Flappy Bird");

		startBtn.setOnAction(e -> {
			bird.fly();
			running = true;
			stack.getChildren().remove(startBtn);
		});

		Scene scene = new Scene(createScene());
		scene.setFill(new ImagePattern(new Image(FlappyBird.class.getResource("background.png").toString())));

		scene.setOnMouseClicked(e -> {
			if (running == false) {
				stack.getChildren().remove(startBtn);
			}
			if (!bird.dead) {
				bird.fly();
				running = true;

			} else {
				// Reset game //
				resetGame();

			}

		});
		scene.setOnKeyPressed(e -> {
			switch (e.getCode()) {
				case SPACE:
					if (!bird.dead) {

						bird.fly();
						running = true;
					} else {
						// Reset game //
						resetGame();
					}
					break;
			}
		}
		);
		stage.setResizable(false);

		stage.setScene(scene);

		stage.show();

	}

	private class Bird extends Sprite {

		private float velocity;
		private float direction = 0;
		private ArrayList<ImagePattern> animation = new ArrayList();
		private MediaPlayer wingSound = new MediaPlayer(new Media(FlappyBird.class.getResource("sound/sfx_wing.wav").toString()));
		private MediaPlayer hitSound = new MediaPlayer(new Media(FlappyBird.class.getResource("sound/sfx_hit.wav").toString()));
		private MediaPlayer dieSound = new MediaPlayer(new Media(FlappyBird.class.getResource("sound/sfx_die.wav").toString()));
		private MediaPlayer pointSound = new MediaPlayer(new Media(FlappyBird.class.getResource("sound/sfx_point.wav").toString()));

		private void addAnimation() {
			animation.add(new ImagePattern(new Image(FlappyBird.class.getResource("1.png").toString())));
			animation.add(new ImagePattern(new Image(FlappyBird.class.getResource("1.png").toString())));
		}

		Bird(int x, int y, int w, int h) {
			super(x, y, w, h, "bird");
			velocity = 0;
			direction = 0;
			addAnimation();
			this.setFill(animation.get(0));
		}

		public Bird() {
			super(0, 0, 0, 0, null);
		}

		public void scoreUp() {
			pointSound.stop();
			pointSound.play();
		}

		public void die() {
			this.dead = true;
			hitSound.stop();
			hitSound.play();
			dieSound.stop();
			dieSound.play();
			velocity = 0;
			direction = 0;

		}

		public void fly() {
			wingSound.stop();
			wingSound.play();

			direction = -60;
			this.setRotate(direction);
			if (this.getTranslateY() > 20.0) {
				velocity = 10;
				setTranslateY(getTranslateY() - velocity);
			} else {
				velocity = 1;

			}

		}

		public void fall() {
			velocity -= 1;
			direction += 5;
			if (direction < 80) {
				this.setRotate(direction);
			}
			setTranslateY(getTranslateY() - velocity);
		}

		public ArrayList<ImagePattern> getAnimation() {
			return animation;
		}
	}

	private static class Sprite extends Rectangle {

		boolean dead = false;
		String type;

		Sprite(int x, int y, int w, int h, String type, Color color) {
			super(w, h, color);
			this.type = type;
			setTranslateX(x);
			setTranslateY(y);

		}

		Sprite(int x, int y, int w, int h, String type) {
			super(w, h);
			this.type = type;
			setTranslateX(x);
			setTranslateY(y);

		}

		public String getType() {
			return this.type;
		}

		public void moveLeft() {
			setTranslateX(getTranslateX() - 2);
		}

	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}

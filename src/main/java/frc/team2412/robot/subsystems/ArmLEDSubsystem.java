package frc.team2412.robot.subsystems;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.EnumSet;

public class ArmLEDSubsystem extends SubsystemBase {

	// CONSTANTS

	// in seconds
	public static final int REQUEST_TIMEOUT_DURATION = 15;
	public static final int CONNECT_TIMEOUT_DURATION = 15;

	public static final String IP = "10.24.12.12";

	// enums
	public static enum ColorPresets {
		RED("Red", "#ff0000"),
		ORANGE("Orange", "#ffa500"),
		YELLOW("Yellow", "#ffff00"),
		GREEN("Green", "#00ff00"),
		BLUE("Blue", "#00ffff"),
		PURPLE("Purple", "##bd00ff"),
		WHITE("White", "#e8e8e8"),
		BLACK("Black", "#000000");

		public final String displayName;
		public final String color;

		ColorPresets(String displayName, String color) {
			this.displayName = displayName;
			this.color = color;
		}
	}

	public static enum LEDPresets {
		RAINBOW("Rainbow", 63, 100, "#000000", "#000000", "#000000"),
		FIRE("Fire", 66, 150, "#000000", "#000000", "#000000"),
		ROBOTOTES("Robototes", 2, 150, "#ff0000", "#000000", "#000000"),
		ROBOT_DISABLED("Robot Disabled", 1, 80, "#d6d6d6", "000000", "000000"),
		AUTONOMOUS("Autonomous", 2, 150, "#66ff66", "#000000", "#000000"),
		RED_ALLIANCE("Red Alliance", 2, 150, "#ff0000", "#000000", "#000000"),
		BLUE_ALLIANCE("Blue Alliance", 2, 150, "#0000ff", "#000000", "#000000");

		public final String displayName;
		public final int fx; // index value from https://kno.wled.ge/features/effects/
		public final int fxSpeed;

		// HEX/DEC
		public final String color1;
		public final String color2;
		public final String color3;

		LEDPresets(
				String displayName, int fx, int fxSpeed, String color1, String color2, String color3) {
			this.displayName = displayName;

			this.fx = fx;
			this.fxSpeed = fxSpeed;

			this.color1 = color1;
			this.color2 = color2;
			this.color3 = color3;
		}
	}

	// VARIABLES

	public int enabled;
	public boolean useCustom;

	public int currentEffect;
	public int effectSpeed;
	public String currentColor1;
	public String currentColor2;
	public String currentColor3;

	// web request

	private HttpClient client;
	private HttpRequest request;

	// shuffleboard

	private SendableChooser enableLEDChooser;
	private SendableChooser presetChooser;
	private SendableChooser color1Chooser;
	private SendableChooser color2Chooser;
	private SendableChooser color3Chooser;

	// CONSTRUCTOR

	public ArmLEDSubsystem() {
		setLEDPreset(LEDPresets.ROBOT_DISABLED);

		try {
			client =
					HttpClient.newBuilder()
							.connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_DURATION))
							.build();
		} catch (Exception e) {
			System.out.println("Failed to initialize http client");
			System.out.println(e.getMessage());
		}
		try {
			request = getRequest();
		} catch (Exception e) {
			System.out.println("Failed to initialize http request");
			System.out.println(e.getMessage());
		}

		// shuffleboard

		ShuffleboardTab armLedTab = Shuffleboard.getTab("Arm LED");

		// add options
		// itereates through each enum and adds it to the preset chooser table.
		EnumSet.allOf(LEDPresets.class)
				.forEach(
						ledPreset ->
								presetChooser.addOption(
										ledPreset.displayName, new InstantCommand(() -> setLEDPreset(ledPreset))));
		EnumSet.allOf(ColorPresets.class)
				.forEach(
						colorPreset ->
								color1Chooser.addOption(
										colorPreset.displayName,
										new InstantCommand(
												() -> {
													currentColor1 = colorPreset.color;
													updateLED();
												})));
		EnumSet.allOf(ColorPresets.class)
				.forEach(
						colorPreset ->
								color2Chooser.addOption(
										colorPreset.displayName,
										new InstantCommand(
												() -> {
													currentColor2 = colorPreset.color;
													updateLED();
												})));
		EnumSet.allOf(ColorPresets.class)
				.forEach(
						colorPreset ->
								color3Chooser.addOption(
										colorPreset.displayName,
										new InstantCommand(
												() -> {
													currentColor3 = colorPreset.color;
													updateLED();
												})));

		enableLEDChooser.setDefaultOption("Activated", new InstantCommand(() -> enableLED(true)));
		enableLEDChooser.addOption("Deactivated", new InstantCommand(() -> enableLED(false)));

		// add choosers to shuffleboard
		armLedTab.add("LED Preset", presetChooser).withPosition(2, 0).withSize(2, 1);
		armLedTab.add("Color 1", color1Chooser).withPosition(0, 0).withSize(2, 1);
		armLedTab.add("Color 2", color2Chooser).withPosition(0, 1).withSize(2, 1);
		armLedTab.add("Color 3", color3Chooser).withPosition(0, 2).withSize(2, 1);
		armLedTab.add("LED enabled", enableLEDChooser).withPosition(4, 0).withSize(2, 1);

		// gulp
		updateLED();
	}

	// METHODS

	/*
	 * Enables/Disables the LED.
	 * @param enable Enables if true, disables if false.
	 */
	public void enableLED(boolean activate) {
		if (activate) {
			enabled = 1;
		} else {
			enabled = 0;
		}
	}
	/*
	 * Sets LED to autonomous mode (green)
	 */
	public void setLEDAutonomous() {
		setLEDPreset(LEDPresets.AUTONOMOUS);
	}

	/** Called during Teleop to set LED to red or blue based off alliance. */
	public void setLEDAlliance() {
		if (DriverStation.getAlliance() == DriverStation.Alliance.Red) {
			// red
			setLEDPreset(LEDPresets.RED_ALLIANCE);

		} else {
			// blue
			setLEDPreset(LEDPresets.BLUE_ALLIANCE);
		}
	}

	/*
	 * Sets the LED to a preset
	 * @param preset The preset to set the led to
	 */
	public void setLEDPreset(LEDPresets preset) {
		currentEffect = preset.fx;
		effectSpeed = preset.fxSpeed;
		currentColor1 = preset.color1;
		currentColor2 = preset.color2;
		currentColor3 = preset.color3;

		updateLED();
	}

	public HttpRequest getRequest() {
		String uri =
				"http://"
						+ IP
						+ "/win&T="
						+ enabled
						+ "&FX="
						+ currentEffect
						+ "&SX="
						+ effectSpeed
						+ "&CL="
						+ currentColor1
						+ "&CL2="
						+ currentColor2
						+ "&CL3="
						+ currentColor3;

		return HttpRequest.newBuilder()
				.timeout(Duration.ofSeconds(REQUEST_TIMEOUT_DURATION))
				.uri(URI.create(uri))
				.build();
	}

	public void updateLED() {
		request = getRequest();
		client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
	}
}

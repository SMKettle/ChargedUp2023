package frc.team2412.robot.subsystems;

import static frc.team2412.robot.Hardware.*;
import static frc.team2412.robot.subsystems.ArmSubsystem.ArmConstants.*;
import static frc.team2412.robot.subsystems.ArmSubsystem.ArmConstants.PositionType.*;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxAbsoluteEncoder;
import com.revrobotics.SparkMaxPIDController;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.team2412.robot.Robot;

public class ArmSubsystem extends SubsystemBase {

	// Constants

	public static class ArmConstants { // To find values later
		// Mech problem basically

		// pounds/lbs
		public static final double INNER_ARM_MASS = 4.69;
		public static final double OUTER_ARM_MASS = 5.03;
		public static final double INTAKE_MASS = 5.57;

		// Center of mass stuff

		// inches
		public static final double INNER_ARM_CENTER_OF_MASS_DISTANCE_FROM_JOINT = 11.218;
		public static final double OUTER_ARM_CENTER_OF_MASS_DISTANCE_FROM_JOINT = 15.712;
		public static final double WRIST_CENTER_OF_MASS_DISTANCE_FROM_JOINT =
				10.85; // TODO: find distance
		public static final double WRIST_CENTER_OF_MASS_ANGLE_OFFSET = -20.15; // TODO: find offset

		public static final double INNER_ARM_LENGTH = 25;
		public static final double OUTER_ARM_LENGTH = 27.25;

		public static final double SHOULDER_ENCODER_TO_ARM_ANGLE_RATIO = 750 / 1;
		public static final double WRIST_ENCODER_TO_WRIST_ANGLE_RATIO = 24 / 1;
		public static final double SHOULDER_ELBOW_GEAR_RATIO = 68 / 48;

		// PID
		// TODO: FIND ALL VALUES
		public static final double ARM_K_P = 0;
		public static final double ARM_K_I = 0;
		public static final double ARM_K_D = 0;

		public static final double WRIST_DEFAULT_P = 0;
		public static final double WRIST_DEFAULT_I = 0;
		public static final double WRIST_DEFAULT_D = 0;

		// Feed Forward
		public static final double ARM_K_A = 0;
		public static final double ARM_K_G = 0;
		public static final double ARM_K_S = 0;
		public static final double ARM_K_V = 0;

		public static final double WRIST_K_A = 0;
		public static final double WRIST_K_G = 0;
		public static final double WRIST_K_S = 0;
		public static final double WRIST_K_V = 0;

		// Constraints

		public static final float ARM_FORWARD_LIMIT = 118 * (float) SHOULDER_ENCODER_TO_ARM_ANGLE_RATIO;
		public static final float ARM_REVERSE_LIMIT = 6 * (float) SHOULDER_ENCODER_TO_ARM_ANGLE_RATIO;
		public static final float WRIST_FORWARD_LIMIT =
				307 * (float) WRIST_ENCODER_TO_WRIST_ANGLE_RATIO; // TODO: might not need ratio? figure out
		public static final float WRIST_REVERSE_LIMIT = 62 * (float) WRIST_ENCODER_TO_WRIST_ANGLE_RATIO;

		public static final int MIN_PERCENT_OUTPUT = -1;
		public static final int MAX_PERCENT_OUTPUT = 1;

		public static final double ARM_POS_TOLERANCE = 0.01;
		public static final double WRIST_POS_TOLERANCE = 0.01;

		public static final double ARM_VELOCITY_TOLERANCE = 0.2;
		public static final double WRIST_VELOCITY_TOLERANCE = 0.2;

		public static final double MAX_ARM_VELOCITY = 5;
		public static final double MAX_ARM_ACCELERATION = 5;

		public static final double MAX_WRIST_VELOCITY = 5;
		public static final double MAX_WRIST_ACCELERATION = 5;

		public static final Constraints ARM_CONSTRAINTS =
				new Constraints(MAX_ARM_ACCELERATION, MAX_ARM_VELOCITY);
		public static final Constraints WRIST_CONSTRAINTS =
				new Constraints(MAX_WRIST_ACCELERATION, MAX_WRIST_VELOCITY);

		public static final double TICKS_TO_INCHES = 42 / 360; // Maybe unneeded?

		// TODO: find limits
		public static final double MIN_ARM_ANGLE = 56.9;
		public static final double MAX_ARM_ANGLE = 170;
		public static final double MIN_WRIST_ANGLE = 20;
		public static final double MAX_WRIST_ANGLE = 100;

		// ENUM

		/*
		 * TODO:
		 * Find arm and wrist angles (see onenote page)
		 *
		 * Values are angles that correspond to specific arm positions:
		 * Arm Angle
		 * Retracted Wrist Angle
		 * Retracted Wrist Angle (cone edition)
		 * Prescore Wrist Angle
		 * Scoring Wrist Angle
		 */
		public static enum PositionType {
			UNKNOWN_POSITION(0, 0, 0, 0, 0),
			ARM_LOW_POSITION(0, 62, 72, 0, 262.63),
			ARM_MIDDLE_POSITION(87.42, 54, 72, 142, 262.63),
			ARM_HIGH_POSITION(118, 54, 72, 180, 263),
			ARM_SUBSTATION_POSITION(80, 54, 72, 0, 0); // ?

			public final double armAngle;
			public final double retractedWristAngle;
			public final double retractedConeWristAngle;
			public final double prescoringWristAngle;
			public final double scoringWristAngle;

			PositionType(
					double armAngle,
					double retractedWristAngle,
					double retractedConeWristAngle,
					double prescoringWristAngle,
					double scoringWristAngle) {
				this.armAngle = armAngle;
				this.retractedWristAngle = retractedWristAngle;
				this.retractedConeWristAngle = retractedConeWristAngle;
				this.prescoringWristAngle = prescoringWristAngle;
				this.scoringWristAngle = scoringWristAngle;
			}
		}
	}

	// Hardware

	private PositionType currentPosition;
	private boolean manualOverride;
	private double armGoal;
	private double wristGoal;

	private final CANSparkMax armMotor1;
	private final CANSparkMax armMotor2;
	private final CANSparkMax wristMotor;

	private final SparkMaxAbsoluteEncoder shoulderEncoder;
	private final SparkMaxAbsoluteEncoder wristEncoder;

	private final ProfiledPIDController armPID;
	private final SparkMaxPIDController wristPID;

	private final ArmFeedforward wristFeedforward;

	// Constructor

	public ArmSubsystem() {
		armMotor1 = new CANSparkMax(ARM_MOTOR1, MotorType.kBrushless);
		armMotor2 = new CANSparkMax(ARM_MOTOR2, MotorType.kBrushless);
		wristMotor = new CANSparkMax(WRIST_MOTOR, MotorType.kBrushless);

		shoulderEncoder = armMotor1.getAbsoluteEncoder(SparkMaxAbsoluteEncoder.Type.kDutyCycle);
		wristEncoder = wristMotor.getAbsoluteEncoder(SparkMaxAbsoluteEncoder.Type.kDutyCycle);

		armPID = new ProfiledPIDController(ARM_K_P, ARM_K_I, ARM_K_D, ARM_CONSTRAINTS);
		wristPID = wristMotor.getPIDController();

		wristFeedforward = new ArmFeedforward(WRIST_K_A, WRIST_K_G, WRIST_K_S, WRIST_K_V);

		currentPosition = UNKNOWN_POSITION;
		manualOverride = false;

		// armPID.reset(getShoulderAngle());
		// wristPID.reset(getWristAngle());
	}

	// Methods

	public void configMotors() {

		armMotor1.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, true);
		armMotor1.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, true);
		armMotor1.setSoftLimit(CANSparkMax.SoftLimitDirection.kForward, ARM_FORWARD_LIMIT);
		armMotor1.setSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, ARM_REVERSE_LIMIT);
		armMotor1.setSmartCurrentLimit(10);

		wristMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, true);

		armMotor2.follow(armMotor1, true);
		setWristPID(WRIST_DEFAULT_P, WRIST_DEFAULT_I, WRIST_DEFAULT_D);
	}

	public void setManualOverride(boolean override) {
		manualOverride = override;
	}

	public void setArmPID(double p, double i, double d) {
		armPID.setP(p);
		armPID.setI(i);
		armPID.setD(d);
	}

	public void setWristPID(double p, double i, double d) {
		wristPID.setP(p);
		wristPID.setI(i);
		wristPID.setD(d);
	}

	public void setArmMotor(double percentOutput) {
		percentOutput =
				MathUtil.clamp(MAX_ARM_VELOCITY * percentOutput, MIN_PERCENT_OUTPUT, MAX_PERCENT_OUTPUT);
		armMotor1.set(percentOutput);
	}

	public void setWristMotor(double percentOutput) {
		percentOutput =
				MathUtil.clamp(MAX_WRIST_VELOCITY * percentOutput, MIN_PERCENT_OUTPUT, MAX_PERCENT_OUTPUT);
		wristPID.setReference(
				percentOutput, CANSparkMax.ControlType.kDutyCycle, 0, calculateWristFeedforward());
	}

	public void setPosition(PositionType position) {
		currentPosition = position;
	}

	public void setArmGoal(double targetAngle) {
		targetAngle = MathUtil.clamp(targetAngle, MIN_ARM_ANGLE, MAX_ARM_ANGLE);
		armPID.setGoal(targetAngle);
	}

	public void setWristGoal(double targetAngle) {
		// wristPID.setGoal(MathUtil.clamp(targetAngle, MIN_WRIST_ANGLE, MAX_WRIST_ANGLE));
		wristPID.setReference(
				targetAngle * SHOULDER_ENCODER_TO_ARM_ANGLE_RATIO,
				CANSparkMax.ControlType.kPosition,
				0,
				convertToVolts(calculateWristFeedforward()));
		wristGoal = targetAngle;
	}

	public double calculateArmPID() {
		return armPID.calculate(getShoulderAngle(), armPID.getGoal());
	}

	// public double calculateWristPID() {
	// 	return armPID.calculate(getWristAngle(), wristPID.getGoal());
	// }

	public double calculateArmFeedforward() {
		return ARM_K_S * Math.signum(armGoal - getShoulderAngle())
				+ ARM_K_G * getAngleTowardsCenterOfMass()
				+ ARM_K_V * 0
				+ ARM_K_A * 0;
	}

	public double calculateWristFeedforward() {
		return wristFeedforward.calculate(getShoulderAngle() - getElbowAngle() + getWristAngle(), 0);
	}

	public double getShoulderAngle() {
		return shoulderEncoder.getPosition() / SHOULDER_ENCODER_TO_ARM_ANGLE_RATIO;
	}

	public double getElbowAngle() {
		return shoulderEncoder.getPosition() * SHOULDER_ELBOW_GEAR_RATIO;
	}

	public double getWristAngle() {
		return wristEncoder.getPosition();
	}

	public PositionType getPosition() {
		return currentPosition;
	}

	public double convertToVolts(double percentOutput) {
		return percentOutput * Robot.getInstance().getVoltage();
	}

	public double getAngleTowardsCenterOfMass() {

		// Get Coordinates of Centers of Mass
		double innerArmCenterOfMassX =
				INNER_ARM_CENTER_OF_MASS_DISTANCE_FROM_JOINT
						* Math.cos(Math.toRadians(180 - getShoulderAngle()));
		double innerArmCenterOfMassY =
				INNER_ARM_CENTER_OF_MASS_DISTANCE_FROM_JOINT
						* Math.sin(Math.toRadians(180 - getShoulderAngle()));

		double outerArmCenterOfMassX =
				OUTER_ARM_CENTER_OF_MASS_DISTANCE_FROM_JOINT
								* Math.cos(Math.toRadians(getElbowAngle() - getShoulderAngle()))
						+ INNER_ARM_LENGTH * Math.cos(Math.toRadians(180 - getShoulderAngle()));
		double outerArmCenterOfMassY =
				OUTER_ARM_CENTER_OF_MASS_DISTANCE_FROM_JOINT
								* Math.sin(Math.toRadians(getElbowAngle() - getShoulderAngle()))
						+ INNER_ARM_LENGTH * Math.sin(Math.toRadians(180 - getShoulderAngle()));

		double intakeCenterOfMassX =
				WRIST_CENTER_OF_MASS_DISTANCE_FROM_JOINT
								* Math.cos(
										Math.toRadians(180 - (getWristAngle() + WRIST_CENTER_OF_MASS_ANGLE_OFFSET)))
						+ OUTER_ARM_LENGTH * Math.cos(Math.toRadians(getElbowAngle() - getShoulderAngle()))
						+ INNER_ARM_LENGTH * Math.cos(Math.toRadians(180 - getShoulderAngle()));
		double intakeCenterOfMassY =
				WRIST_CENTER_OF_MASS_DISTANCE_FROM_JOINT
								* Math.sin(
										Math.toRadians(180 - (getWristAngle() + WRIST_CENTER_OF_MASS_ANGLE_OFFSET)))
						+ OUTER_ARM_LENGTH * Math.sin(Math.toRadians(getElbowAngle() - getShoulderAngle()))
						+ INNER_ARM_LENGTH * Math.sin(Math.toRadians(180 - getShoulderAngle()));

		// Calculate Average Center of Mass of Arm + Intake
		double centerOfMassX =
				(INNER_ARM_MASS * innerArmCenterOfMassX
								+ OUTER_ARM_MASS * outerArmCenterOfMassX
								+ INTAKE_MASS * intakeCenterOfMassX)
						/ (INNER_ARM_MASS + OUTER_ARM_MASS + INTAKE_MASS);
		double centerOfMassY =
				(INNER_ARM_MASS * innerArmCenterOfMassY
								+ OUTER_ARM_MASS * outerArmCenterOfMassY
								+ INTAKE_MASS * intakeCenterOfMassY)
						/ (INNER_ARM_MASS + OUTER_ARM_MASS + INTAKE_MASS);

		// Return Angle needed to face Center of Mass
		return Math.atan2(centerOfMassY, centerOfMassX);
	}

	@Override
	public void periodic() {
		// Periodic Arm movement for Preset Angle Control
		if (!manualOverride) {

			armMotor1.setVoltage(
					convertToVolts(
							MathUtil.clamp(
									calculateArmPID() + calculateArmFeedforward(),
									MIN_PERCENT_OUTPUT,
									MAX_PERCENT_OUTPUT)));

			wristPID.setReference(
					wristGoal, CANSparkMax.ControlType.kPosition, 0, calculateWristFeedforward());
			// TODO: figure out PID Slot?
		}
	}
}

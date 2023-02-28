package frc.team2412.robot.commands.intake;

import static frc.team2412.robot.subsystems.IntakeSubsystem.IntakeConstants.INTAKE_OUT_SPEED;

import frc.team2412.robot.subsystems.IntakeSubsystem;

public class IntakeSetOutCommand extends IntakeSetCommand {

	public IntakeSetOutCommand(IntakeSubsystem intakeSubsystem) {
		super(intakeSubsystem, INTAKE_OUT_SPEED);
	}

	// TODO: uncomment when color matching works
	// @Override
	// public boolean isFinished() {
	// 	return (intakeSubsystem.detectType() == GamePieceType.NONE || !intakeSubsystem.hasObject());
	// }

}

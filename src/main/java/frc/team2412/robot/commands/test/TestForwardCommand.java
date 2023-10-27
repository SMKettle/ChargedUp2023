package frc.team2412.robot.commands.test;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.team2412.robot.subsystems.TestSubsystem;

public class TestForwardCommand extends CommandBase {
    
    // variables 

    private TestSubsystem testSubsystem;
    // constructor

    public TestForwardCommand(TestSubsystem testSubsystem) {
        this.testSubsystem = testSubsystem;

        // makes it so you cant use multiple commands at once in a subsystem
        addRequirements(testSubsystem);
    }

    // methods

    @Override
    public void initialize() {
        // code here
        testSubsystem.setMotorForward(); 

    }

    @Override
    public void execute() {
        // loops set of code on robot
    }

    @Override
    public void end(boolean interrupted) {
        // runs when command stops
    }

    @Override
    public boolean isFinished() {
        return true;
    }


}

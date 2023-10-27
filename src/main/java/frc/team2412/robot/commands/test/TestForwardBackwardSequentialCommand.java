package frc.team2412.robot.commands.test;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.team2412.robot.subsystems.TestSubsystem;
import edu.wpi.first.wpilibj2.command.WaitCommand;

public class TestForwardBackwardSequentialCommand extends SequentialCommandGroup {
    
    // variables

    private TestSubsystem testSubsystem;

    // CONSTRUCTOR !!

    public TestForwardBackwardSequentialCommand(TestSubsystem testSubsystem) {
        this.testSubsystem = testSubsystem;

        // adds the commands in order 4!!!!!!!!!!!!!!
        addCommands(new TestForwardCommand(testSubsystem), new WaitCommand(4),new TestBackwardCommand(testSubsystem));
    }




}

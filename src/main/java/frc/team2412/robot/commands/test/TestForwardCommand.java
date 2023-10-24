package frc.team2412.robot.commands.test;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.team2412.robot.subsystems.TestSubsystem;

public class TestForwardCommand extends CommandBase {
    
    // variables 

    private TestSubsystem testSubsystem;
    // constructor

    public TestForwardCommand(TestSubsystem testSubsystem) {
        this.testSubsystem = testSubsystem;


        addRequirements(testSubsystem);
    }



    // methods




}

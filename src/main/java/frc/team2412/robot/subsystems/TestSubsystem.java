package frc.team2412.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import com.revrobotics.SparkMaxAbsoluteEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;


public class TestSubsystem extends SubsystemBase{
    private final SparkMaxAbsoluteMax motorEncoder;
    public final CANSparkMax motor;
    

    //CONSTANT VARIABLES (YES ALL CAPS)
    private final double MAX_ANGLE = 60
    ;



    public TestSubsystem(CANSparkMax motor) {
        this.motor = new CANSparkmax(0, MotorType.kBrushless);
        this.motorEncoder = motor.getAbsoluteEncoder();


    }



    public double getSpeed() {
        return motor.get(); //gives us motor speed
    }



    public double getAngle() {
        return motor.getPosition();
    }

    public void setMotorReverse() {
        motor.set(-MOTOR_SPEED);
    }

    public void setMotorForward() {
        motor.set(MOTOR_SPEED);
    }

    public void periodic() {
        System.out.println(getAngle());
    }


}

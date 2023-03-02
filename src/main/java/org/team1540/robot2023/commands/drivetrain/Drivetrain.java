package org.team1540.robot2023.commands.drivetrain;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.kauailabs.navx.frc.AHRS;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.commands.PPSwerveControllerCommand;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.team1540.robot2023.Constants;
import org.team1540.robot2023.LimelightManager;
import org.team1540.robot2023.utils.swerve.SwerveModule;

import static org.team1540.robot2023.Constants.Swerve;
import static org.team1540.robot2023.Globals.field2d;

public class Drivetrain extends SubsystemBase {

    private SwerveModuleState[] states = new SwerveModuleState[]{new SwerveModuleState(), new SwerveModuleState(), new SwerveModuleState(), new SwerveModuleState()};
    private final SwerveModule[] modules = new SwerveModule[]{
            new SwerveModule(0, Swerve.Mod0.constants),
            new SwerveModule(1, Swerve.Mod1.constants),
            new SwerveModule(2, Swerve.Mod2.constants),
            new SwerveModule(3, Swerve.Mod3.constants)
    };

    private final AHRS gyro = new AHRS(SPI.Port.kMXP);

    private static final boolean isGyroReversed = true;

    // These PID controllers don't actually do anything, but their PID values are copied for PathPlanner commands
    private final PIDController dummyTranslationPID = new PIDController(Constants.Auto.PID.translationP,Constants.Auto.PID.translationI,Constants.Auto.PID.translationD);
    private final PIDController dummyRotationPID = new PIDController(Constants.Auto.PID.rotationP,Constants.Auto.PID.rotationI,Constants.Auto.PID.rotationD);
    // Whether to allow the wheels to park
    private boolean isParkMode = false;

    // Odometry
    private final SwerveDrivePoseEstimator poseEstimator = new SwerveDrivePoseEstimator(Swerve.swerveKinematics, getYaw(), getModulePositions(), new Pose2d());

    public Drivetrain() {
        PPSwerveControllerCommand.setLoggingCallbacks(
                (trajectory) -> field2d.getObject("activetrajectory").setTrajectory(trajectory),
                (pose) -> field2d.getObject("targetpose").setPose(pose),
                null,
                (translation, rotation) -> {
                    SmartDashboard.putNumber("drivetrain/pathplanner/xErr", translation.getX());
                    SmartDashboard.putNumber("drivetrain/pathplanner/yErr", translation.getY());
                    SmartDashboard.putNumber("drivetrain/pathplanner/rotErr", rotation.getDegrees());
                });
        SmartDashboard.putData("drivetrain/translationPID", dummyTranslationPID);
        SmartDashboard.putData("drivetrain/rotationPID", dummyRotationPID);
        SmartDashboard.putNumberArray("drivetrain/swerveModuleStates/desired", new double[]{
                states[0].angle.getDegrees(), states[0].speedMetersPerSecond,
                states[1].angle.getDegrees(), states[1].speedMetersPerSecond,
                states[2].angle.getDegrees(), states[2].speedMetersPerSecond,
                states[3].angle.getDegrees(), states[3].speedMetersPerSecond
        });
        SmartDashboard.putNumberArray("drivetrain/swerveModuleStates/actual", new double[]{
                modules[0].getState().angle.getDegrees(), modules[0].getState().speedMetersPerSecond,
                modules[1].getState().angle.getDegrees(), modules[1].getState().speedMetersPerSecond,
                modules[2].getState().angle.getDegrees(), modules[2].getState().speedMetersPerSecond,
                modules[3].getState().angle.getDegrees(), modules[3].getState().speedMetersPerSecond
        });
        SmartDashboard.putNumber("drivetrain/gyro", getYaw().getDegrees());
        gyro.reset();
    }

    @Override
    public void periodic() {

        SwerveDriveKinematics.desaturateWheelSpeeds(states, Swerve.maxVelocity);
        modules[0].setDesiredState(states[0], true, isParkMode);
        modules[1].setDesiredState(states[1], true, isParkMode);
        modules[2].setDesiredState(states[2], true, isParkMode);
        modules[3].setDesiredState(states[3], true, isParkMode);
        poseEstimator.update(getYaw(), getModulePositions());
        LimelightManager.getInstance().applyEstimates(poseEstimator);

        field2d.setRobotPose(poseEstimator.getEstimatedPosition());
    }


    public void resetAllToAbsolute() {
        DataLogManager.log("Zeroing swerve module relative encoders");
        for (SwerveModule module: modules) {
            module.resetToAbsolute();
        }
    }

    /**
     * Adjusts all the wheels to achieve the desired movement
     *
     * @param xPercent      The forward and backward movement
     * @param yPercent      The left and right movement
     * @param rotPercent           The amount to turn
     * @param fieldRelative If the directions are relative to the field instead of the robot
     */
    public void drive(double xPercent, double yPercent, double rotPercent, boolean fieldRelative) {

        double xSpeed = xPercent * Swerve.maxVelocity;
        double ySpeed = yPercent * Swerve.maxVelocity;
        double rot = Math.toRadians(rotPercent*360);
        ChassisSpeeds chassisSpeeds = fieldRelative
                ? ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, rot, gyro.getRotation2d())
                : new ChassisSpeeds(xSpeed, ySpeed, rot);
        double deadzone = 0.02;
        double rotDeadzone = 0.1;
        if (Math.abs(xPercent) > 0 || Math.abs(yPercent) > deadzone || Math.abs(rot) > rotDeadzone) {
            isParkMode = false;
            setChassisSpeeds(chassisSpeeds);
        } else {
            stopLocked();
        }
    }

    /**
     * Stops the robot and forms an X with the wheels
     */
    public void stopLocked() {
        isParkMode = true;
        setModuleStates(new SwerveModuleState[]{
                new SwerveModuleState(0, Rotation2d.fromDegrees(45)), //Front Left
                new SwerveModuleState(0, Rotation2d.fromDegrees(-45)), //Front Right
                new SwerveModuleState(0, Rotation2d.fromDegrees(-45)), //Back Left
                new SwerveModuleState(0, Rotation2d.fromDegrees(45)) //Back Right
        });
    }

    void setModuleStates(SwerveModuleState[] newStates) {
        this.states = newStates;
    }

    private void setChassisSpeeds(ChassisSpeeds speeds) {
        states = Swerve.swerveKinematics.toSwerveModuleStates(speeds);
    }


    protected Command getPathCommand(PathPlannerTrajectory trajectory) {
        return new PPSwerveControllerCommand(
                trajectory,
                this::getPose, // Pose supplier
                // TODO: Tune
                new PIDController(dummyTranslationPID.getP(), dummyTranslationPID.getI(), dummyTranslationPID.getD()),
                new PIDController(dummyTranslationPID.getP(), dummyTranslationPID.getI(), dummyTranslationPID.getD()),
                new PIDController(dummyRotationPID.getP(), dummyRotationPID.getI(), dummyRotationPID.getD()),
                this::setChassisSpeeds, // Module states consumer
                this // Requires this drive subsystem
        );
    }

    protected Command getResettingPathCommand(PathPlannerTrajectory trajectory) {
        return new SequentialCommandGroup(
                new InstantCommand(() -> resetOdometry(trajectory.getInitialHolonomicPose())),
                getPathCommand(trajectory)
        );
    }


    /**
     * Sets the gyroscope angle to zero. This can be used to set the direction the robot is currently facing to the
     * 'forwards' direction.
     */
    public void zeroGyroscope() {
        gyro.zeroYaw();
    } //todo: make sure this doesn't break odometry

    public Rotation2d getYaw() {
        if (gyro.isMagnetometerCalibrated()) {
            // We will only get valid fused headings if the magnetometer is calibrated
            return Rotation2d.fromDegrees(gyro.getFusedHeading());
        }
        // We have to invert the angle of the NavX so that rotating the robot counter-clockwise makes the angle increase.
        return Rotation2d.fromDegrees(360.0 - gyro.getYaw());
    }

    public Rotation2d getPitch() {
        return Rotation2d.fromDegrees(gyro.getPitch());
    }

    public void setNeutralMode(NeutralMode neutralMode) {
        for (SwerveModule module : modules) {
            module.setNeutralMode(neutralMode);
        }
    }
    public Rotation2d getRoll() {
        return Rotation2d.fromDegrees(gyro.getRoll());
    }

    public Pose2d getPose() {
        return poseEstimator.getEstimatedPosition();
    }

    public void resetOdometry(Pose2d pose) {
        poseEstimator.resetPosition(getYaw(), getModulePositions(), pose);
    }

    public double getHeading() {
        return Math.IEEEremainder(gyro.getAngle(), 360) * (isGyroReversed ? -1.0 : 1.0);
      }

    public SwerveModulePosition[] getModulePositions(){
        return new SwerveModulePosition[]{
                modules[0].getPosition(),
                modules[1].getPosition(),
                modules[2].getPosition(),
                modules[3].getPosition()
        };
    }

}

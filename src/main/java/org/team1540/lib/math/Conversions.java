package org.team1540.lib.math;

import edu.wpi.first.math.geometry.Rotation2d;

public class Conversions {
//
//    /**
//     * @param positionCounts CANCoder Position Counts
//     * @param gearRatio Gear Ratio between CANCoder and Mechanism
//     * @return Degrees of Rotation of Mechanism
//     */
//    public static double CANcoderToDegrees(double positionCounts, double gearRatio) {
//        return positionCounts * (360.0 / (gearRatio * 4096.0));
//    }
//
//    /**
//     * @param degrees Degrees of rotation of Mechanism
//     * @param gearRatio Gear Ratio between CANCoder and Mechanism
//     * @return CANCoder Position Counts
//     */
//    public static double degreesToCANcoder(double degrees, double gearRatio) {
//        return degrees / (360.0 / (gearRatio * 4096.0));
//    }
//
//    /**
//     * @param counts Falcon Position Counts
//     * @param gearRatio Gear Ratio between Falcon and Mechanism
//     * @return Degrees of Rotation of Mechanism
//     */
//    public static double falconToDegrees(double positionCounts, double gearRatio) {
//        return positionCounts * (360.0 / (gearRatio * 2048.0));
//    }
//
//    /**
//     * @param degrees Degrees of rotation of Mechanism
//     * @param gearRatio Gear Ratio between Falcon and Mechanism
//     * @return Falcon Position Counts
//     */
//    public static double degreesToFalcon(double degrees, double gearRatio) {
//        return degrees / (360.0 / (gearRatio * 2048.0));
//    }

    public static double degreesToRotations(double degrees, double gearRatio) {
        return degrees / (360.0 / gearRatio);
    }
    /**
     * @param rotations The number of rotations on the falcon
     * @param gearRatio Gear Ratio between Falcon and Mechanism
     * @return Degrees of Rotation of Mechanism
     */
    public static double rotationsToDegrees(double rotations, double gearRatio) {
        return rotations * (360.0 / gearRatio);
    }

    /**
     * @param rotationsPerSecond Falcon Velocity Counts
     * @param gearRatio Gear Ratio between Falcon and Mechanism (set to 1 for Falcon RPM)
     * @return RPM of Mechanism
     */
    public static double falconToRPM(double rotationsPerSecond, double gearRatio) {
        double motorRPM = rotationsPerSecond/60;
        double mechRPM = motorRPM / gearRatio;
        return mechRPM;
    }

    /**
     * @param RPM RPM of mechanism
     * @param gearRatio Gear Ratio between Falcon and Mechanism (set to 1 for Falcon RPM)
     * @return RPM of Mechanism
     */
    public static double RPMToFalcon(double RPM, double gearRatio) {
        double motorRPM = RPM * gearRatio;
        return motorRPM / 60;
    }

    /**
     * @param rotationsPerSecond Falcon Velocity Counts
     * @param circumference Circumference of Wheel
     * @param gearRatio Gear Ratio between Falcon and Mechanism (set to 1 for Falcon MPS)
     * @return Falcon Velocity Counts
     */
    public static double falconToMPS(double rotationsPerSecond, double circumference, double gearRatio){
        double wheelRPM = falconToRPM(rotationsPerSecond, gearRatio);
        double wheelMPS = (wheelRPM * circumference) / 60;
        return wheelMPS;
    }
//
    /**
     * @param velocity Velocity MPS
     * @param circumference Circumference of Wheel
     * @param gearRatio Gear Ratio between Falcon and Mechanism (set to 1 for Falcon MPS)
     * @return Falcon RPS
     */
    public static double MPSToFalcon(double velocity, double circumference, double gearRatio){
        double wheelRPM = ((velocity * 60) / circumference);
        return RPMToFalcon(wheelRPM, gearRatio);
    }
//
    /**
     * @param positionCounts Falcon Position Counts
     * @param circumference Circumference of Wheel
     * @param gearRatio Gear Ratio between Falcon and Wheel
     * @return Meters
     */
    public static double falconToMeters(double rotations, double circumference, double gearRatio){
        return rotations * (circumference / gearRatio);
    }
//
//    /**
//     * @param meters Meters
//     * @param circumference Circumference of Wheel
//     * @param gearRatio Gear Ratio between Falcon and Wheel
//     * @return Falcon Position Counts
//     */
//    public static double MetersToFalcon(double meters, double circumference, double gearRatio){
//        return meters / (circumference / (gearRatio * 2048.0));
//    }

    /**
     * @param cartesianAngle angle in the Cartesian angle system
     * @return angle in the actual (slightly scuffed) angle system
     */
    public static Rotation2d cartesianToActual(Rotation2d cartesianAngle) {
        double theta = cartesianAngle.getDegrees();
        return Rotation2d.fromDegrees(theta - 90 < -180 ? theta + 270 : theta - 90);
    }

    /**
     * @param actualAngle angle in the actual (slightly scuffed) angle system
     * @return angle in the Cartesian angle system
     */
    public static Rotation2d actualToCartesian(Rotation2d actualAngle) {
        double theta = actualAngle.getDegrees();
        return Rotation2d.fromDegrees(theta + 90 > 180 ? theta - 270 : theta + 90);
    }
}
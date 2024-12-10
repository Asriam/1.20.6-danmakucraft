class THObject {

    constructor(container:Entity,position:Vec3){
    }

    initPosition(position) {
        // Initialize the position
    }

    shoot(speed, vectorRotation) {
        // Launch the object with speed and vector rotation
    }

    shoot(velocity) {
        // Launch the object with a given velocity
    }

    shoot(speed, rotation, isDeg) {
        // Launch the object with speed, rotation, and degrees
    }

    injectScript(script) {
        // Set a script to be used by the object
    }

    spawn() {
        // Add this object to the object manager
    }

    setDead() {
        // Mark the object as dead
    }

    remove() {
        // Set the object for removal
    }

    setPosition(pos) {
        // Set the position of the object
    }

    setPosition(x, y, z) {
        // Set the position using x, y, z coordinates
    }

    setScale(scale) {
        // Set the scale of the object
    }

    setSize(size) {
        // Set the size of the hitbox
    }

    setVelocity(velocity, setRotation) {
        // Set the velocity and optionally set rotation
    }

    setVelocityFromDirection(speed, direction, setRotation) {
        // Set velocity based on speed and direction
    }

    setVelocityFromRotation(speed, rotation, isDeg, setRotation) {
        // Set velocity using speed, rotation, and degree/radian
    }

    setAcceleration(acceleration) {
        // Set the acceleration of the object
    }

    setAccelerationFromDirection(acceleration, direction) {
        // Set acceleration based on a direction
    }

    setAccelerationFromRotation(acceleration, rotation, isDeg) {
        // Set acceleration using rotation
    }

    setRotation(xRot, yRot, zRot) {
        // Set the rotation using x, y, z angles
    }

    setRotation(rotation) {
        // Set the rotation using a vector
    }

    setRotation(rotation) {
        // Set the rotation using a vector
    }

    setRotationByDirectionalVector(vectorRotation) {
        // Set rotation based on directional vector
    }

    static VectorAngleToRadAngle(formDir) {
        // Convert a vector angle to radian angle
    }

    static VectorAngleToRadAngleInverseX(formDir) {
        // Convert a vector angle to radian angle with inverse x
    }

    static VectorAngleToEulerDegAngle(formDir) {
        // Convert vector angle to Euler degree angle
    }

    setColor(color) {
        // Set the color
    }

    setColor(r, g, b, a) {
        // Set the color using RGBA values
    }

    setLifetime(time) {
        // Set the lifetime of the object
    }

    getTimer() {
        // Return the current timer value
    }

    getColor() {
        // Return the color
    }

    getPosition() {
        // Return the current position
    }

    getPrePosition() {
        // Return the previous position
    }

    getOffsetPosition(partialTicks) {
        // Get the object position with an offset
    }
}
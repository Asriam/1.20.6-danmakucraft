#version 150

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

in vec3 Position;
in vec3 Position2;
in vec3 Rotation;
in vec3 Scale;

out vec3 EllipsoidPosition;
out vec3 EllipsoidRotation;
out vec3 EllipsoidRadius;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    EllipsoidPosition = Position2;
    EllipsoidRotation = Rotation;
    EllipsoidRadius = Scale;
}

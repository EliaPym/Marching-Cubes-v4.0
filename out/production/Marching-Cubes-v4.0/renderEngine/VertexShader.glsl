#version 150 core

in vec3 position;
in vec3 colour;

out vec3 vertexColour;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main(){
    vertexColour = colour;
    mat4 pvm = projection * view * model;
    gl_Position = pvm * vec4(position, 1.0);
}
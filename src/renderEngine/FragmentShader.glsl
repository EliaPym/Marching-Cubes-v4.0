#version 330

in vec3 vertexColour;
in vec3 vertexNormal;
in vec3 fragPos;

uniform vec3 lightPos;

out vec4 fragColour;

void main(){
    vec3 lightColour = vec3(1.0f, 1.0f, 1.0f);

    float ambientStrength = 0.5f;
    vec3 ambient = ambientStrength * lightColour;

    vec3 norm = normalize(vertexNormal);
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = (diff * lightColour) * 1;

    vec3 result = (ambient + diffuse) * vertexColour;
    fragColour = vec4(result, 1.0);
}
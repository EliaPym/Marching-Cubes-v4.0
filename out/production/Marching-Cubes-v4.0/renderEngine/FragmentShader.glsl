#version 330

in vec3 vertexColour;
in vec3 vertexNormal;
in vec3 fragPos;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform float ambientStrength;
uniform float specularStrength;

out vec4 fragColour;

void main(){
    vec3 lightColour = vec3(1.0f, 1.0f, 1.0f);

    // ambient lighting
    vec3 ambient = ambientStrength * lightColour;

    // diffuse lighting
    vec3 norm = -normalize(vertexNormal);
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColour;

    // specular lighting
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 64);
    vec3 specular = specularStrength * spec * lightColour;

    // phong shading
    vec3 result = (ambient + diffuse + specular) * vertexColour;
    fragColour = vec4(result, 1.0);
}
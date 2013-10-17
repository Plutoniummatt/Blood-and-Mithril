#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;


uniform float dayLight;
uniform vec2 lightSource;
uniform vec2 resolution;
uniform float size;
uniform vec4 color;

float lighting() {
	vec2 position = v_texCoords * resolution;
	float dist = length(position - lightSource);
	
	return max(1 - dist*2/size, 0);
}

void main()
{
	float lighting = lighting();
	gl_FragColor = vec4(texture2D(u_texture, v_texCoords)) * vec4(1, 1, 1, lighting) * color;
}
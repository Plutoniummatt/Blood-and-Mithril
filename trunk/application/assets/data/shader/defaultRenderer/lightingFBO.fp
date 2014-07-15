#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;

uniform float intensity;
uniform vec2 position;
uniform vec2 resolution;
uniform vec4 color;

void main()
{
	float dist = distance(v_texCoords * resolution, position);
	float factor = min(1.0, 10 * intensity / dist / dist);
	
	gl_FragColor = texture2D(u_texture2, v_texCoords) + color * vec4(1.0, 1.0, 1.0, factor);
}
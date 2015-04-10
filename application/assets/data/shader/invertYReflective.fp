#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;
uniform float time;

void main()
{
	vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
	vec4 sample1 = texture2D(u_texture, inverted);
	
	gl_FragColor = sample1;
}
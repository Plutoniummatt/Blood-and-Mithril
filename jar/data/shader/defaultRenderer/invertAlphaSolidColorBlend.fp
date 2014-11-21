#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture_2;

void main()
{
	vec2 invertedCoords = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
	vec4 a = texture2D(u_texture, invertedCoords);
	vec4 b = texture2D(u_texture_2, invertedCoords);
	gl_FragColor = a + b;
}
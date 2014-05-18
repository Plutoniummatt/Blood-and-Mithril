#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;


void main()
{
	vec4 first = texture2D(u_texture, v_texCoords);
	vec4 second = texture2D(u_texture2, v_texCoords);
	
	gl_FragColor = vec4(
		first.r,
		first.g,
		first.b,
		1.0
	);
}
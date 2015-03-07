#ifdef GL_ES
#define LOWP lowp
precision highp float;
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
		max(first.r, second.r),
		max(first.g, second.g),
		max(first.b, second.b),
		1.0
	);
}
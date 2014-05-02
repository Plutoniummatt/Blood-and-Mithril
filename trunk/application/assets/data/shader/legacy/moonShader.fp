#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;


void main()
{
	vec4 sample = texture2D(u_texture, v_texCoords);
	gl_FragColor = sample  * 2.0 * vec4(vec3(0.9, 0.8, 0.65), sample.r);
}
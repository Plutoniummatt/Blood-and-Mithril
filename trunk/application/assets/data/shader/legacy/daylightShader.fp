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
	vec4 color = texture2D(u_texture, v_texCoords);
	vec4 color2 = texture2D(u_texture2, v_texCoords);
	
	gl_FragColor = vec4(color.rgb * color2.a, color.a) * vec4(color2.rgb, 1.0);
}
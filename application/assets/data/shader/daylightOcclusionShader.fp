#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 res;

uniform vec4 dl;

void main()
{
	vec4 col = texture2D(u_texture, v_texCoords);
	if (col.a == 0.0) {
		col = vec4(0.0, 0.0, 1.0, 1.0);
	}
	gl_FragColor = col;
}
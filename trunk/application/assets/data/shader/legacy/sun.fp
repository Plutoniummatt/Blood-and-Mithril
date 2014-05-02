#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float time;

void main()
{
	float r;
	float g;
	float b;
	float a;

	if (time < 10.0) {
		r = 0.1 + 1.2 * exp(-0.100*pow((time - 10.0), 2.0));
		g = 0.1 + 1.2 * exp(-0.150*pow((time - 10.0), 2.0));
		b = 0.1 + 1.2 * exp(-0.200*pow((time - 10.0), 2.0));
		a = 0.6 + 0.2 * exp(-0.200*pow((time - 10.0), 2.0));
	} else if (time >= 10 && time < 14) {
		r = 1.3;
		g = 1.3;
		b = 1.3;
		a = 0.8;
	} else {
		r = 0.1 + 1.2 * exp(-0.100*pow((time - 14.0), 2.0));
		g = 0.1 + 1.2 * exp(-0.150*pow((time - 14.0), 2.0));
		b = 0.1 + 1.2 * exp(-0.200*pow((time - 14.0), 2.0));
		a = 0.6 + 0.2 * exp(-0.200*pow((time - 14.0), 2.0));
	}

  gl_FragColor = texture2D(u_texture, v_texCoords) * vec4(r, g, b, a);
}
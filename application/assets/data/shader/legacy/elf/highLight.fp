#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float alpha;
uniform vec3 eyeColor;
uniform int hair;
uniform vec3 hairColor;

void main()
{
	vec4 color = texture2D(u_texture, v_texCoords);
	if (color.r > 0.003821568 && color.r < 0.004021568 && color.g == 0.0 && color.b == 0.0 && color.a != 0.0) {
		color = vec4(eyeColor, 1);
	}
	if (hair == 1) {
		color = vec4(color.rbg * hairColor, color.a);
	}
	gl_FragColor = color * (vec4(alpha, alpha, alpha, 1) + vec4(0.5, 0.5, 0.5, 0));
}
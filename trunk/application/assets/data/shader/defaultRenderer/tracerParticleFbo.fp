#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;

uniform vec2 p1;
uniform vec2 p2;
uniform float intensity;
uniform vec2 resolution;
uniform vec4 color;

void main()
{
	float l = distance(p2, p1);
	float x = dot((resolution * v_texCoords) - p1, normalize(p2 - p1));
	
	float dist = 1.0;
	if (x <= 0.0) {
		dist = distance(p1, resolution * v_texCoords);
	} else if (x >= l) {
		dist = distance(p2, resolution * v_texCoords);
	} else if (length(p2 - p1) != 0.0) {
		dist = length(
			cross(
				vec3((resolution * v_texCoords) - p1, 0.0), 
				vec3(normalize(p2 - p1), 0.0)
			)
		);
	} else {
		dist = distance(p2, resolution * v_texCoords);
	}

	float factor = min(1.0, 5.0 * intensity / dist / dist);
	
	gl_FragColor = texture2D(u_texture2, v_texCoords) + color * vec4(1.0, 1.0, 1.0, factor);
}
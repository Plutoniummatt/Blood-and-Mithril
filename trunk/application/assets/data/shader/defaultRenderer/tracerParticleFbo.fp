#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec2 currentPosition[256];
uniform vec2 previousPosition[256];
uniform vec4 color[256];
uniform float intensity[256];
uniform vec2 resolution;

void main()
{

	vec4 totalColor = vec4(0.0, 0.0, 0.0, 0.0);
	for (int index = 0; index < 256; index++) {
	
		vec2 p2 = currentPosition[index];
		vec2 p1 = previousPosition[index];
		
		if (length(p2) != 0.0) {
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
			
			vec4 toAdd = color[index] * intensity[index] / dist;
			
			totalColor = vec4(
				max((totalColor.r + toAdd.r) / 2.0, totalColor.r),
				max((totalColor.g + toAdd.g) / 2.0, totalColor.g),
				max((totalColor.b + toAdd.b) / 2.0, totalColor.b),
				totalColor.a + toAdd.a
			);
		} else {
			break;
		}
	}

	gl_FragColor = totalColor;
}
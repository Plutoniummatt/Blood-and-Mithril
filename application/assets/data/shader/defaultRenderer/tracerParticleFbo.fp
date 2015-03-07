#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec2 currentPosition[100];
uniform vec2 previousPosition[100];
uniform vec4 color[100];
uniform float intensity[100];
uniform vec2 resolution;

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main()
{

	vec4 totalColor = vec4(0.0, 0.0, 0.0, 0.0);
	for (int index = 0; index < 100; index++) {
	
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
			
			float rnd = rand(v_texCoords.xy) / 35.0;
			vec4 toAdd = (color[index] * intensity[index] / dist) + vec4(rnd, rnd, rnd, rnd);
			
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
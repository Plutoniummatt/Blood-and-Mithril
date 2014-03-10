#define PI 3.14

//inputs from vertex shader
varying vec2 v_texCoords;

//uniform values
uniform sampler2D u_texture;
uniform vec2 resolution;
uniform vec2 span;

//alpha threshold for our occlusion map
const float THRESHOLD = 0.90;

void main(void) {
	float distance = 1.0;
	
	if (v_texCoords.x > span.x && v_texCoords.x < span.y) {
		for (float y=0.0; y<resolution.y; y+=2.0) {
			//rectangular to polar filter
			vec2 norm = vec2(v_texCoords.x, y/resolution.y) * 2.0 - 1.0;
			float theta = PI*1.5 + norm.x * PI; 
			float r = (1.0 + norm.y) * 0.5;
			
			//coord which we will sample from occlude map
			vec2 coord = vec2(-r * sin(theta), -r * cos(theta))/2.0 + 0.5;
			
			//sample the occlusion map
			vec4 data = texture2D(u_texture, coord);
			
			//the current distance is how far from the top we've come
			float dst = y/resolution.y - (2/resolution.x);
			
			//if we've hit an opaque fragment (occluder), then get new distance
			//if the new distance is below the current, then we'll use that for our ray
			float caster = data.a;
			if (caster > THRESHOLD) {
				distance = min(distance, dst);
				//NOTE: we could probably use "break" or "return" here
			}
		}
	} else {
		distance = 0.0;
	}
	
	gl_FragColor = vec4(vec3(distance), 1.0);
}
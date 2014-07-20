#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D occlusion;
uniform sampler2D occlusion2;
uniform sampler2D occlusion3;
uniform vec4 dayLightColor;
uniform vec4 waterColor;
uniform float waterLevel;
uniform float height;
uniform float falloffDepth;

void main()
{
  vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
  
	float factor1 = texture2D(occlusion, inverted).g;
	float factor2 = texture2D(occlusion2, inverted).r;
	float factor = factor1 * factor2;
  
	vec4 fg = texture2D(u_texture, inverted);
	vec4 lighting = texture2D(occlusion3, v_texCoords);
	vec4 sum = fg * (vec4(factor, factor, factor, 1.0) * dayLightColor + lighting) * vec4(factor1, factor1, factor1, 1.0);
  
	if (v_texCoords.y > waterLevel) {
		float attenuation = max(0.0, (falloffDepth - (v_texCoords.y - waterLevel) * height)) / falloffDepth;
		vec4 attenuationVector = vec4(attenuation, attenuation, attenuation, 1.0) + lighting;
		sum = sum * waterColor * attenuationVector + (vec4(factor, factor, factor, 1.0) * waterColor * (1.0 - fg.a * 0.8) * vec4(1.0, 1.0, 1.0, 0.2) * attenuationVector);
	}
  
  gl_FragColor = sum;
}
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
uniform vec4 dayLightColor;

void main()
{
  vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
  float factor = texture2D(occlusion, inverted).r;
  float shadowFactor = texture2D(occlusion2, inverted).r * 0.35;
  gl_FragColor = texture2D(u_texture, inverted) * vec4(factor, factor, factor, 1.0) * dayLightColor - vec4(shadowFactor, shadowFactor, shadowFactor, 0.0);
}
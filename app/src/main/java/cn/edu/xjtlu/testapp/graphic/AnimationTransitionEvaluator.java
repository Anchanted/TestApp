package cn.edu.xjtlu.testapp.graphic;

import android.animation.TypeEvaluator;

public class AnimationTransitionEvaluator implements TypeEvaluator<AnimationTransition> {
    @Override
    public AnimationTransition evaluate(float fraction, AnimationTransition startValue, AnimationTransition endValue) {
        float currentTranslateX = startValue.translateX + fraction * (endValue.translateX - startValue.translateX);
        float currentTranslateY = startValue.translateY + fraction * (endValue.translateY - startValue.translateY);
        float currentScaleX = startValue.scaleX + fraction * (endValue.scaleX - startValue.scaleX);
        float currentScaleY = startValue.scaleY + fraction * (endValue.scaleY - startValue.scaleY);
        return new AnimationTransition(currentTranslateX, currentTranslateY, currentScaleX, currentScaleY);
    }
}
